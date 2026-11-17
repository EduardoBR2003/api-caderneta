package br.com.api_caderneta.lojas;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loja/configuracao")
public class LojaConfiguracaoController {
  private final LojaConfiguracaoService configuracao;

  public LojaConfiguracaoController(LojaConfiguracaoService configuracao) {
    this.configuracao = configuracao;
  }

  @GetMapping
  public ConfiguracaoLoja obter() {
    return configuracao.obter();
  }

  @PutMapping
  public ConfiguracaoLoja salvar(@RequestBody ConfiguracaoLoja req) {
    return configuracao.salvar(req);
  }
}
