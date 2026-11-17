package br.com.api_caderneta.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class EmailSmtp implements EnviarEmail {
  private final JavaMailSenderImpl smtp = new JavaMailSenderImpl();
  private final String remetente;
  public EmailSmtp(@Value("${CADERNETA_SMTP_HOST:}") String host,
      @Value("${CADERNETA_SMTP_PORT:587}") int porta,
      @Value("${CADERNETA_SMTP_USUARIO:}") String usuario,
      @Value("${CADERNETA_SMTP_SENHA:}") String senha,
      @Value("${CADERNETA_SMTP_REMETENTE:}") String remetente,
      @Value("${CADERNETA_SMTP_STARTTLS:true}") boolean tls) {
    this.remetente = remetente;
    smtp.setHost(host); smtp.setPort(porta); smtp.setUsername(usuario); smtp.setPassword(senha);
    var p = smtp.getJavaMailProperties();
    p.setProperty("mail.smtp.auth", Boolean.toString(!usuario.isBlank()));
    p.setProperty("mail.smtp.starttls.enable", Boolean.toString(tls));
    p.setProperty("mail.smtp.starttls.required", Boolean.toString(tls));
    p.setProperty("mail.smtp.connectiontimeout", "5000");
    p.setProperty("mail.smtp.timeout", "5000");
    p.setProperty("mail.smtp.writetimeout", "5000");
  }
  public void enviar(String destino, String assunto, String mensagem) {
    if (smtp.getHost() == null || smtp.getHost().isBlank() || remetente.isBlank())
      throw new IllegalStateException("Envio de e-mail não configurado.");
    var msg = new SimpleMailMessage();
    msg.setFrom(remetente); msg.setTo(destino); msg.setSubject(assunto); msg.setText(mensagem);
    smtp.send(msg);
  }
}
