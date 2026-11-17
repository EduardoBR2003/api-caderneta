package br.com.api_caderneta.email;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConfirmacoesEmailJdbc implements ConfirmacoesEmail {
  private final JdbcTemplate jdbc;

  public ConfirmacoesEmailJdbc(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public SituacaoEmail situacao(long loja) {
    return jdbc.queryForObject(
        "SELECT email_contato,email_confirmado FROM lojas WHERE id=?",
        (r, n) -> new SituacaoEmail(r.getString(1), r.getBoolean(2)),
        loja);
  }

  @Override
  public boolean bloquearLoja(long loja) {
    return !jdbc.queryForList("SELECT id FROM lojas WHERE id=? FOR UPDATE", Long.class, loja)
        .isEmpty();
  }

  @Override
  public LocalDateTime ultimoEnvio(long loja) {
    var envios =
        jdbc.queryForList(
            "SELECT enviado_em FROM confirmacoes_email WHERE loja_id=?", Timestamp.class, loja);
    return envios.isEmpty() ? null : envios.getFirst().toLocalDateTime();
  }

  @Override
  public void salvarPendente(
      long loja, String email, String hash, LocalDateTime expira, LocalDateTime enviado) {
    jdbc.update("UPDATE lojas SET email_contato=?,email_confirmado=false WHERE id=?", email, loja);
    jdbc.update(
        "INSERT INTO confirmacoes_email(loja_id,token_hash,email,expira_em,enviado_em)"
            + " VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE"
            + " token_hash=VALUES(token_hash),email=VALUES(email),expira_em=VALUES(expira_em),enviado_em=VALUES(enviado_em)",
        loja,
        hash,
        email,
        expira,
        enviado);
  }

  @Override
  public Optional<String> emailValido(long loja, String hash, LocalDateTime agora) {
    return jdbc
        .queryForList(
            "SELECT email FROM confirmacoes_email WHERE loja_id=? AND token_hash=? AND expira_em>?",
            String.class,
            loja,
            hash,
            agora)
        .stream()
        .findFirst();
  }

  @Override
  public boolean confirmar(long loja, String email) {
    return jdbc.update(
            "UPDATE lojas SET email_confirmado=true WHERE id=? AND email_contato=?", loja, email)
        == 1;
  }

  @Override
  public void remover(long loja) {
    jdbc.update("DELETE FROM confirmacoes_email WHERE loja_id=?", loja);
  }
}
