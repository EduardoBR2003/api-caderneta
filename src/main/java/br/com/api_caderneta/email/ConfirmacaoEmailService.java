package br.com.api_caderneta.email;

import br.com.api_caderneta.audit.AuditoriaService;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ConflictException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;

@Service
public class ConfirmacaoEmailService {
  public record Situacao(String email, boolean confirmado) {}
  private final JdbcTemplate jdbc;
  private final EnviarEmail envio;
  private final String site;
  private final AuditoriaService auditoria;
  public ConfirmacaoEmailService(JdbcTemplate jdbc, EnviarEmail envio, AuditoriaService auditoria,
      @Value("${CADERNETA_URL_PUBLICA:http://localhost:3000}") String site) {
    this.jdbc = jdbc; this.envio = envio; this.auditoria = auditoria;
    this.site = site.replaceAll("/+$", "");
  }
  @Transactional(readOnly=true)
  public Situacao situacao(long loja) {
    return jdbc.queryForObject("SELECT email_contato,email_confirmado FROM lojas WHERE id=?",
        (r,n) -> new Situacao(r.getString(1), r.getBoolean(2)), loja);
  }
  static String hash(String token) {
    try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
    catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
  }
  /** Serializa por loja. Trocar endereço revoga o token anterior; envio só após commit. */
  @Transactional
  public void solicitar(long loja, String endereco) {
    String email = endereco == null ? "" : endereco.strip().toLowerCase(java.util.Locale.ROOT);
    if (email.length() > 100 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
      throw new BusinessException("Informe um e-mail válido.");
    jdbc.queryForObject("SELECT id FROM lojas WHERE id=? FOR UPDATE", Long.class, loja);
    var agora = LocalDateTime.now(ZoneOffset.UTC);
    var ultimas = jdbc.queryForList("SELECT enviado_em FROM confirmacoes_email WHERE loja_id=?", java.sql.Timestamp.class, loja);
    if (!ultimas.isEmpty() && ultimas.getFirst().toLocalDateTime().isAfter(agora.minusMinutes(1)))
      throw new ConflictException("Aguarde um minuto antes de solicitar outro e-mail.");
    var atual = situacao(loja);
    if (atual.confirmado() && email.equalsIgnoreCase(atual.email())) return;
    byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
    String token = HexFormat.of().formatHex(bytes);
    jdbc.update("UPDATE lojas SET email_contato=?,email_confirmado=false WHERE id=?", email, loja);
    jdbc.update("INSERT INTO confirmacoes_email(loja_id,token_hash,email,expira_em,enviado_em) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE token_hash=VALUES(token_hash),email=VALUES(email),expira_em=VALUES(expira_em),enviado_em=VALUES(enviado_em)", loja, hash(token), email, agora.plusHours(24), agora);
    Runnable enviar = () -> {
      try { envio.enviar(email, "Confirme o e-mail da sua loja — Caderneta",
          "Confirme o endereço para usar sua loja. O link vale por 24 horas:\n" + site + "/confirmar-email#loja=" + loja + "&token=" + token + "\nSe você não solicitou o cadastro, ignore esta mensagem."); }
      catch (RuntimeException e) {
        // Nunca registrar endereço, corpo, credenciais SMTP ou token.
        org.slf4j.LoggerFactory.getLogger(getClass()).warn("Confirmação pendente: envio indisponível. lojaId={}", loja);
      }
    };
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      public void afterCommit() { enviar.run(); }
    });
  }
  @Transactional
  public void confirmar(long loja, String token) {
    if (token == null || !token.matches("[a-f0-9]{64}")) throw new BusinessException("Link inválido ou expirado.");
    if (jdbc.queryForList("SELECT id FROM lojas WHERE id=? FOR UPDATE", Long.class, loja).isEmpty()) throw new BusinessException("Link inválido ou expirado.");
    var linhas = jdbc.queryForList("SELECT email FROM confirmacoes_email WHERE loja_id=? AND token_hash=? AND expira_em>?", loja, hash(token), LocalDateTime.now(ZoneOffset.UTC));
    if (linhas.isEmpty()) throw new BusinessException("Link inválido ou expirado.");
    int alteradas = jdbc.update("UPDATE lojas SET email_confirmado=true WHERE id=? AND email_contato=?", loja, linhas.getFirst().get("email"));
    if (alteradas != 1) throw new BusinessException("Link inválido ou expirado.");
    jdbc.update("DELETE FROM confirmacoes_email WHERE loja_id=?", loja);
    auditoria.registrarComo(loja, null, "confirmacao-email", "PUBLICO", "EMAIL_CONFIRMADO", "LOJA", loja, java.util.Map.of());
  }
}
