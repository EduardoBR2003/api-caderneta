package br.com.api_caderneta.email;

import br.com.api_caderneta.audit.AuditoriaService;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ConflictException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;

@Service
public class ConfirmacaoEmailService {
  private final ConfirmacoesEmail confirmacoes;
  private final EnviarEmail envio;
  private final String site;
  private final AuditoriaService auditoria;

  public ConfirmacaoEmailService(
      ConfirmacoesEmail confirmacoes,
      EnviarEmail envio,
      AuditoriaService auditoria,
      @Value("${CADERNETA_URL_PUBLICA:http://localhost:3000}") String site) {
    this.confirmacoes = confirmacoes;
    this.envio = envio;
    this.auditoria = auditoria;
    this.site = site.replaceAll("/+$", "");
  }

  @Transactional(readOnly = true)
  public SituacaoEmail situacao(long loja) {
    return confirmacoes.situacao(loja);
  }

  static String hash(String token) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  /** Serializa por loja. Trocar endereço revoga o token anterior; envio só após commit. */
  @Transactional
  public void solicitar(long loja, String endereco) {
    String email = endereco == null ? "" : endereco.strip().toLowerCase(java.util.Locale.ROOT);
    if (email.length() > 100 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
      throw new BusinessException("Informe um e-mail válido.");
    confirmacoes.bloquearLoja(loja);
    var agora = LocalDateTime.now(ZoneOffset.UTC);
    var ultimo = confirmacoes.ultimoEnvio(loja);
    if (ultimo != null && ultimo.isAfter(agora.minusMinutes(1)))
      throw new ConflictException("Aguarde um minuto antes de solicitar outro e-mail.");
    var atual = situacao(loja);
    if (atual.confirmado() && email.equalsIgnoreCase(atual.email())) return;
    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    String token = HexFormat.of().formatHex(bytes);
    confirmacoes.salvarPendente(loja, email, hash(token), agora.plusHours(24), agora);
    Runnable enviar =
        () -> {
          try {
            envio.enviar(
                email,
                "Confirme o e-mail da sua loja — Caderneta",
                "Confirme o endereço para usar sua loja. O link vale por 24 horas:\n"
                    + site
                    + "/confirmar-email#loja="
                    + loja
                    + "&token="
                    + token
                    + "\nSe você não solicitou o cadastro, ignore esta mensagem.");
          } catch (RuntimeException e) {
            // Nunca registrar endereço, corpo, credenciais SMTP ou token.
            org.slf4j.LoggerFactory.getLogger(getClass())
                .warn("Confirmação pendente: envio indisponível. lojaId={}", loja);
          }
        };
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          public void afterCommit() {
            enviar.run();
          }
        });
  }

  @Transactional
  public void confirmar(long loja, String token) {
    if (token == null || !token.matches("[a-f0-9]{64}"))
      throw new BusinessException("Link inválido ou expirado.");
    if (!confirmacoes.bloquearLoja(loja)) throw new BusinessException("Link inválido ou expirado.");
    var email =
        confirmacoes
            .emailValido(loja, hash(token), LocalDateTime.now(ZoneOffset.UTC))
            .orElseThrow(() -> new BusinessException("Link inválido ou expirado."));
    if (!confirmacoes.confirmar(loja, email))
      throw new BusinessException("Link inválido ou expirado.");
    confirmacoes.remover(loja);
    auditoria.registrarComo(
        loja,
        null,
        "confirmacao-email",
        "PUBLICO",
        "EMAIL_CONFIRMADO",
        "LOJA",
        loja,
        java.util.Map.of());
  }
}
