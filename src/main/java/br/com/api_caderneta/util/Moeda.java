package br.com.api_caderneta.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class Moeda {
  private Moeda() {}

  public static String formatar(BigDecimal valor) {
    var formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
    formato.setMinimumFractionDigits(2);
    formato.setMaximumFractionDigits(2);
    return "R$ " + formato.format(valor);
  }
}
