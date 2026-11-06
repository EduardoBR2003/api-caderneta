package br.com.api_caderneta.util;

import br.com.api_caderneta.exceptions.BusinessException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Regras compartilhadas pelas entradas HTTP e pelos serviços. */
public final class Regras {
  private Regras() {}

  public static BigDecimal dinheiro(BigDecimal valor, boolean zero) {
    if (valor == null || (zero ? valor.signum() < 0 : valor.signum() <= 0))
      throw new BusinessException(
          zero ? "Informe um valor maior ou igual a zero." : "Informe um valor maior que zero.");
    try {
      var v = valor.setScale(2, RoundingMode.UNNECESSARY);
      if (v.precision() > 12) throw new ArithmeticException();
      return v;
    } catch (ArithmeticException e) {
      throw new BusinessException(
          "O valor deve ter até 10 dígitos inteiros e duas casas decimais.");
    }
  }

  public static int prazo(Integer prazo) {
    if (prazo == null || prazo < 0 || prazo > 3650)
      throw new BusinessException("O prazo deve estar entre 0 e 3650 dias.");
    return prazo;
  }

  public static String nome(String nome) {
    if (nome == null || nome.trim().length() < 3 || nome.trim().length() > 150)
      throw new BusinessException("O nome deve ter de 3 a 150 caracteres.");
    return nome.trim();
  }

  public static String cpf(String cpf) {
    String c = cpf == null ? "" : cpf.replaceAll("[.\\-\\s]", "");
    if (!c.matches("\\d{11}") || c.chars().distinct().count() == 1)
      throw new BusinessException("CPF inválido.");
    for (int j = 9; j < 11; j++) {
      int soma = 0;
      for (int i = 0; i < j; i++) soma += (c.charAt(i) - '0') * (j + 1 - i);
      int digito = (soma * 10) % 11;
      if (digito == 10) digito = 0;
      if (digito != c.charAt(j) - '0') throw new BusinessException("CPF inválido.");
    }
    return c;
  }
}
