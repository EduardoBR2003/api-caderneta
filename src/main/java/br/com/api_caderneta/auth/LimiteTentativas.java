package br.com.api_caderneta.auth;

import br.com.api_caderneta.util.Fuso;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bloqueio temporário após falhas repetidas de login ou de ativação por convite. O estado fica no banco,
 * então vale para todas as instâncias da API. Logins e IPs são guardados só como hash SHA-256.
 *
 * <p>O IP vem de {@code request.getRemoteAddr()}, que atrás de proxy reflete X-Forwarded-For e pode ser
 * forjado pelo cliente; por isso o limite por login (não forjável) é o controle principal.
 */
@Service
public class LimiteTentativas {

  /** Tentativa recusada por excesso de falhas. */
  public static class Bloqueado extends RuntimeException {
    private final long segundos;

    public Bloqueado(long segundos) {
      super("Muitas tentativas. Aguarde e tente novamente.");
      this.segundos = segundos;
    }

    public long segundos() {
      return segundos;
    }
  }

  private final JdbcTemplate jdbc;
  private final int maxPorLogin;
  private final int maxPorIp;
  private final Duration janela;
  private final Duration bloqueio;

  public LimiteTentativas(
      JdbcTemplate jdbc,
      @Value("${caderneta.login.max-falhas-por-login:5}") int maxPorLogin,
      @Value("${caderneta.login.max-falhas-por-ip:30}") int maxPorIp,
      @Value("${caderneta.login.janela:15m}") Duration janela,
      @Value("${caderneta.login.bloqueio:15m}") Duration bloqueio) {
    this.jdbc = jdbc;
    this.maxPorLogin = maxPorLogin;
    this.maxPorIp = maxPorIp;
    this.janela = janela;
    this.bloqueio = bloqueio;
  }

  public static String chaveLogin(String login) {
    return "l:" + sha256(login == null ? "" : login.trim().toLowerCase(Locale.ROOT));
  }

  public static String chaveIp(String ip) {
    return "i:" + sha256(ip == null ? "" : ip);
  }

  /** Lança {@link Bloqueado} se alguma das chaves estiver bloqueada agora. */
  @Transactional(readOnly = true)
  public void verificar(String... chaves) {
    var agora = Fuso.agoraUtc();
    for (String chave : chaves) {
      List<LocalDateTime> ate =
          jdbc.queryForList(
              "SELECT bloqueado_ate FROM tentativas_acesso WHERE chave=? AND bloqueado_ate>?",
              LocalDateTime.class,
              chave,
              agora);
      if (!ate.isEmpty()) {
        throw new Bloqueado(Math.max(1, Duration.between(agora, ate.get(0)).toSeconds()));
      }
    }
  }

  /** Registra uma falha; ao atingir o limite dentro da janela, bloqueia a chave pelo tempo configurado. */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void registrarFalhaLogin(String login, String ip) {
    registrar(chaveLogin(login), maxPorLogin);
    registrar(chaveIp(ip), maxPorIp);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void registrarFalhaIp(String ip) {
    registrar(chaveIp(ip), maxPorIp);
  }

  /** Login bem-sucedido zera as falhas daquele login (o contador por IP continua). */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void registrarSucesso(String login) {
    jdbc.update("DELETE FROM tentativas_acesso WHERE chave=?", chaveLogin(login));
  }

  private void registrar(String chave, int max) {
    var agora = Fuso.agoraUtc();
    // Garante a linha e a trava (FOR UPDATE) para contagem correta sob concorrência.
    jdbc.update(
        "INSERT IGNORE INTO tentativas_acesso(chave,falhas,janela_inicio,bloqueado_ate) VALUES(?,0,?,NULL)",
        chave,
        agora);
    record Linha(int falhas, LocalDateTime inicio) {}
    var linha =
        jdbc.queryForObject(
            "SELECT falhas, janela_inicio FROM tentativas_acesso WHERE chave=? FOR UPDATE",
            (rs, n) -> new Linha(rs.getInt(1), rs.getObject(2, LocalDateTime.class)),
            chave);
    int falhas = linha.falhas();
    var inicio = linha.inicio();
    if (inicio.isBefore(agora.minus(janela))) {
      falhas = 0;
      inicio = agora;
    }
    falhas++;
    LocalDateTime bloqueadoAte = falhas >= max ? agora.plus(bloqueio) : null;
    if (bloqueadoAte != null) {
      falhas = 0; // nova contagem depois do bloqueio
      inicio = agora;
    }
    jdbc.update(
        "UPDATE tentativas_acesso SET falhas=?, janela_inicio=?, bloqueado_ate=COALESCE(?, bloqueado_ate)"
            + " WHERE chave=?",
        falhas,
        inicio,
        bloqueadoAte,
        chave);
    // Limpeza oportunista de registros antigos.
    var limite = agora.minus(janela).minus(bloqueio);
    jdbc.update(
        "DELETE FROM tentativas_acesso WHERE janela_inicio<? AND (bloqueado_ate IS NULL OR bloqueado_ate<?)"
            + " LIMIT 100",
        limite,
        agora);
  }

  private static String sha256(String s) {
    try {
      return HexFormat.of()
          .formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
