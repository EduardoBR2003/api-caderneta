package br.com.api_caderneta.catalogo;

import br.com.api_caderneta.audit.AuditoriaService;
import br.com.api_caderneta.auth.Dtos.Pagina;
import br.com.api_caderneta.exceptions.*;
import br.com.api_caderneta.model.enums.Perfil;
import br.com.api_caderneta.security.UsuarioAtual;
import br.com.api_caderneta.util.Regras;
import java.math.BigDecimal;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogoService {
  public record ItemDTO(Long id, String descricao, BigDecimal precoSugerido, boolean ativo) {}
  public record Entrada(String descricao, BigDecimal precoSugerido, Boolean ativo) {}
  private final CatalogoRepository itens;
  private final AuditoriaService auditoria;
  public CatalogoService(CatalogoRepository itens, AuditoriaService auditoria) {
    this.itens = itens;
    this.auditoria = auditoria;
  }
  private ItemDTO dto(ItemCatalogo i) { return new ItemDTO(i.getId(), i.getDescricao(), i.getPrecoSugerido(), i.isAtivo()); }
  @Transactional(readOnly = true)
  public Pagina<ItemDTO> listar(int page, int size, String q, boolean incluirInativos) {
    boolean gerente = UsuarioAtual.obrigatorio().getPerfil() == Perfil.GERENTE;
    return Pagina.de(itens.findAll((r, query, cb) -> {
      String termo = q == null ? "" : q.strip().toLowerCase(java.util.Locale.ROOT);
      var descricao = cb.like(cb.lower(r.get("descricao")), "%" + termo.replace("%", "\\%").replace("_", "\\_") + "%", '\\');
      return incluirInativos && gerente ? descricao : cb.and(descricao, cb.isTrue(r.get("ativo")));
    }, PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, size)), Sort.by("descricao", "id"))).map(this::dto));
  }
  @Transactional
  public ItemDTO salvar(Long id, Entrada req) {
    if (UsuarioAtual.obrigatorio().getPerfil() != Perfil.GERENTE)
      throw new org.springframework.security.access.AccessDeniedException("Somente gerente mantém itens frequentes.");
    var i = id == null ? new ItemCatalogo() : itens.findById(id).orElseThrow(() -> new ResourceNotFoundException("Item não encontrado."));
    var antes = dto(i);
    if (req.descricao() == null || req.descricao().strip().isEmpty() || req.descricao().strip().length() > 200)
      throw new BusinessException("Informe uma descrição de até 200 caracteres.");
    var preco = req.precoSugerido() == null ? null : Regras.dinheiro(req.precoSugerido(), false);
    if (preco != null && preco.precision() > 10) throw new BusinessException("Preço fora do limite permitido.");
    i.setDescricao(req.descricao().strip());
    i.setPrecoSugerido(preco);
    if (req.ativo() != null) i.setAtivo(req.ativo());
    i = itens.saveAndFlush(i);
    auditoria.registrar(id == null ? "ITEM_CRIADO" : "ITEM_EDITADO", "CATALOGO", i.getId(),
        AuditoriaService.mudanca("item", antes, dto(i)));
    return dto(i);
  }
}
