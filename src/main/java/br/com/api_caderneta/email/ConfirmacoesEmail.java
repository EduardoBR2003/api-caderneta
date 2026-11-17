package br.com.api_caderneta.email;

import java.time.LocalDateTime;
import java.util.Optional;

/** Persistência necessária ao caso de uso; bloqueios participam da transação do chamador. */
public interface ConfirmacoesEmail {
  SituacaoEmail situacao(long loja);

  boolean bloquearLoja(long loja);

  LocalDateTime ultimoEnvio(long loja);

  void salvarPendente(
      long loja, String email, String hash, LocalDateTime expira, LocalDateTime enviado);

  Optional<String> emailValido(long loja, String hash, LocalDateTime agora);

  boolean confirmar(long loja, String email);

  void remover(long loja);
}
