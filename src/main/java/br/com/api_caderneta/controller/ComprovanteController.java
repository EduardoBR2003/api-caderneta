package br.com.api_caderneta.controller;

import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.repository.*;
import br.com.api_caderneta.security.UsuarioAtual;
import br.com.api_caderneta.services.VendaService;
import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

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

  private String esc(Object s) {
    return HtmlUtils.htmlEscape(String.valueOf(s == null ? "" : s));
  }

  private String dinheiro(java.math.BigDecimal v) {
    return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(v);
  }

  private String pagina(String titulo, String corpo) {
    return "<!doctype html><html lang='pt-BR'><meta charset='utf-8'><meta name='viewport'"
               + " content='width=device-width, initial-scale=1'><title>"
        + esc(titulo)
        + "</title><style>body{font:16px"
        + " Arial;max-width:800px;margin:32px}td,th{padding:8px;text-align:left}table{border-collapse:collapse}"
        + " @media print{button{display:none}}</style><h1>"
        + esc(titulo)
        + "</h1>"
        + corpo
        + "<p>Comprovante de registro na Caderneta. Não substitui documento fiscal.</p><button"
        + " onclick='window.print()'>Imprimir / Salvar PDF</button></html>";
  }

  @GetMapping(value = "/api/vendas/{id}/comprovante", produces = "text/html;charset=UTF-8")
  public String venda(@PathVariable Long id) {
    return vendaHtml(vendas.getVendaById(id));
  }

  private String vendaHtml(br.com.api_caderneta.dto.VendaDTO v) {
    StringBuilder itens =
        new StringBuilder("<table><tr><th>Item</th><th>Quantidade</th><th>Unitário</th></tr>");
    java.math.BigDecimal soma = java.math.BigDecimal.ZERO;
    for (var i : v.getItens()) {
      itens
          .append("<tr><td>")
          .append(esc(i.getDescricaoProduto()))
          .append("</td><td>")
          .append(i.getQuantidade())
          .append("</td><td>")
          .append(dinheiro(i.getPrecoUnitario()))
          .append("</td></tr>");
      soma =
          soma.add(i.getPrecoUnitario().multiply(java.math.BigDecimal.valueOf(i.getQuantidade())));
    }
    itens.append("</table>");
    return pagina(
        "Venda #" + v.getId(),
        "<p>Cliente: "
            + esc(v.getClienteNome())
            + "</p><p>Data: "
            + esc(
                v.getDataHora()
                    .atOffset(java.time.ZoneOffset.UTC)
                    .atZoneSameInstant(br.com.api_caderneta.util.Fuso.NEGOCIO)
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
            + "</p><p>Total: "
            + dinheiro(v.getValorTotal())
            + "</p>"
            + itens
            + "<p>Soma dos itens: "
            + dinheiro(soma)
            + "</p><p>Valor não detalhado: "
            + dinheiro(v.getValorTotal().subtract(soma))
            + "</p>");
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
