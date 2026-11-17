package br.com.api_caderneta.util;

import br.com.api_caderneta.exceptions.BusinessException;

public final class RegrasFiador {
  private RegrasFiador() {}

  /**
   * Cliente não pode ser fiador de si mesmo (o CPF é único por papel, então a regra é explícita).
   */
  public static void exigirDistinto(String cpfCliente, String cpfFiador) {
    var a = cpfCliente == null ? "" : cpfCliente.replaceAll("\\D", "");
    var b = cpfFiador == null ? "" : cpfFiador.replaceAll("\\D", "");
    if (!a.isEmpty() && a.equals(b)) {
      throw new BusinessException("O cliente não pode ser fiador de si mesmo.");
    }
  }
}
