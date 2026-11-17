package br.com.api_caderneta.controller;
import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.web.util.HtmlUtils;
/** Renderização pura; autorização e carregamento pertencem ao controller. */
final class ComprovanteHtml {
  static String esc(Object s) {
    return HtmlUtils.htmlEscape(String.valueOf(s == null ? "" : s));
  }

  static String dinheiro(java.math.BigDecimal v) {
    return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(v);
  }

  static String pagina(String titulo, String corpo) {
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
        + " id='imprimir'>Imprimir / Salvar PDF</button><script src='/api/comprovante.js' defer></script></html>";
  }

  static String vendaHtml(br.com.api_caderneta.dto.VendaDTO v) {
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

}
