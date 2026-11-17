package br.com.api_caderneta.controller;

import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.repository.*;
import br.com.api_caderneta.security.UsuarioAtual;
import br.com.api_caderneta.services.VendaService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import static br.com.api_caderneta.controller.ComprovanteHtml.*;

@RestController
public class ComprovanteController {
  private final VendaService vendas;
  private final PagamentoRepository pagamentos;
  private final VendaRepository repo;

  public ComprovanteController(VendaService v, PagamentoRepository p, VendaRepository r) {
    vendas = v;
    pagamentos = p;
    repo = r;
  }

  @GetMapping(value="/api/comprovante.js", produces="application/javascript")
  public String scriptImpressao() {
    return "document.getElementById('imprimir')?.addEventListener('click', () => window.print());";
  }

  @GetMapping(value = "/api/vendas/{id}/comprovante", produces = "text/html;charset=UTF-8")
  public String venda(@PathVariable Long id) {
    return vendaHtml(vendas.getVendaById(id));
  }

  @GetMapping(value = "/api/me/vendas/{id}/comprovante", produces = "text/html;charset=UTF-8")
  @Transactional(readOnly = true)
  public String compra(@PathVariable Long id) {
    var v =
        repo.findById(id)
            .filter(x -> x.getCliente().getId().equals(UsuarioAtual.obrigatorio().getPessoaId()))
            .orElseThrow(() -> new ResourceNotFoundException("Compra não encontrada."));
    var d = new br.com.api_caderneta.dto.VendaDTO();
    d.setId(v.getId());
    d.setValorTotal(v.getValorTotal());
    d.setDataHora(v.getDataHora());
    d.setClienteNome(v.getCliente().getNome());
    d.setItens(
        v.getItensVenda().stream()
            .map(
                i -> {
                  var t = new br.com.api_caderneta.dto.ItemVendaDTO();
                  t.setDescricaoProduto(i.getDescricaoProduto());
                  t.setQuantidade(i.getQuantidade());
                  t.setPrecoUnitario(i.getPrecoUnitario());
                  return t;
                })
            .toList());
    return vendaHtml(d);
  }

  @GetMapping(
      value = {
        "/api/dividas/{dividaId}/pagamentos/{id}/comprovante",
        "/api/me/dividas/{dividaId}/pagamentos/{id}/comprovante"
      },
      produces = "text/html;charset=UTF-8")
  @Transactional(readOnly = true)
  public String pagamento(@PathVariable Long dividaId, @PathVariable Long id) {
    var p =
        pagamentos
            .findById(id)
            .filter(x -> x.getDivida().getId().equals(dividaId))
            .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado."));
    var u = UsuarioAtual.obrigatorio();
    if (u.getPerfil() == br.com.api_caderneta.model.enums.Perfil.CLIENTE
        && !p.getDivida().getCliente().getId().equals(u.getPessoaId()))
      throw new ResourceNotFoundException("Pagamento não encontrado.");
    return pagina(
        "Recebimento #" + id,
        "<p>Cliente: "
            + esc(p.getDivida().getCliente().getNome())
            + "</p><p>Dívida: #"
            + dividaId
            + "</p><p>Data: "
            + esc(
                p.getDataPagamento()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            + "</p><p>Valor recebido: "
            + dinheiro(p.getValorPago())
            + "</p><p>Método: "
            + esc(
                switch (p.getMetodoPagamento()) {
                  case DINHEIRO -> "Dinheiro";
                  case PIX -> "Pix";
                  case CARTAO_CREDITO -> "Cartão de crédito";
                  case CARTAO_DEBITO -> "Cartão de débito";
                  case BOLETO_BANCARIO -> "Boleto bancário";
                  case TRANSFERENCIA_BANCARIA -> "Transferência bancária";
                })
            + "</p>");
  }
}
