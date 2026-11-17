package br.com.api_caderneta.email;

/** Fronteira de envio: a política de confirmação não conhece SMTP nem credenciais. */
public interface EnviarEmail {
  void enviar(String destino, String assunto, String mensagem);
}
