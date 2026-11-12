package br.com.api_caderneta.auth;

import java.nio.charset.StandardCharsets;

public final class PoliticaSenha {
  private PoliticaSenha() {}

  public static boolean aceita(String senha) {
    return senha != null && !senha.isBlank() && senha.length() >= 8
        && senha.getBytes(StandardCharsets.UTF_8).length <= 72;
  }
}
