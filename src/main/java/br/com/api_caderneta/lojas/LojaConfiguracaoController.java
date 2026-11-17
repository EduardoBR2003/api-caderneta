package br.com.api_caderneta.lojas;

import br.com.api_caderneta.audit.AuditoriaService;
import br.com.api_caderneta.model.enums.Perfil;
import br.com.api_caderneta.security.UsuarioAtual;
import br.com.api_caderneta.util.Regras;
import java.math.BigDecimal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loja/configuracao")
public class LojaConfiguracaoController {
  public record Configuracao(BigDecimal limiteCreditoPadrao) {}
  private final LojaRepository lojas;
  private final AuditoriaService auditoria;
  public LojaConfiguracaoController(LojaRepository lojas, AuditoriaService auditoria) { this.lojas = lojas; this.auditoria = auditoria; }
  @GetMapping @Transactional(readOnly=true)
  public Configuracao obter() { return new Configuracao(lojas.findById(LojaAtual.exigida()).orElseThrow().getLimiteCreditoPadrao()); }
  @PutMapping @Transactional
  public Configuracao salvar(@RequestBody Configuracao req) {
    if (UsuarioAtual.obrigatorio().getPerfil() != Perfil.GERENTE)
      throw new org.springframework.security.access.AccessDeniedException("Somente gerente configura o limite padrão.");
    var loja = lojas.findById(LojaAtual.exigida()).orElseThrow();
    var antes = loja.getLimiteCreditoPadrao();
    var valor = Regras.dinheiro(req.limiteCreditoPadrao(), true);
    if (valor.precision() > 10) throw new br.com.api_caderneta.exceptions.BusinessException("O limite deve ter até 8 dígitos inteiros e duas casas decimais.");
    loja.setLimiteCreditoPadrao(valor);
    lojas.save(loja);
    auditoria.registrar("LIMITE_PADRAO_ALTERADO", "LOJA", loja.getId(), AuditoriaService.mudanca("limiteCreditoPadrao", antes, loja.getLimiteCreditoPadrao()));
    return new Configuracao(loja.getLimiteCreditoPadrao());
  }
}
