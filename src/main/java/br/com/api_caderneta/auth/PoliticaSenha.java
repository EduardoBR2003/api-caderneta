package br.com.api_caderneta.auth;

import java.nio.charset.StandardCharsets;

public final class PoliticaSenha {
  private PoliticaSenha() {}

  public static boolean aceita(String senha) {
    return senha != null && !java.util.Set.of("12345678", "123456789", "1234567890", "password", "password123", "senha123", "qwerty123", "abcdefgh", "11111111", "00000000", "87654321").contains(senha.toLowerCase(java.util.Locale.ROOT)) && !senha.isBlank() && senha.length() >= 8
        && senha.getBytes(StandardCharsets.UTF_8).length <= 72;
  }
}
