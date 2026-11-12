package br.com.api_caderneta.services;

import br.com.api_caderneta.exceptions.*;
import br.com.api_caderneta.security.UsuarioAtual;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.function.Supplier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotenciaService {
  private final JdbcTemplate jdbc;
  private final ObjectMapper mapper;

  public IdempotenciaService(JdbcTemplate j, ObjectMapper m) {
    jdbc = j;
    mapper = m;
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public <T> T executar(String chave, String rota, Object corpo, Class<T> tipo, Supplier<T> acao) {
    if (chave == null || !chave.matches("[A-Za-z0-9-]{16,80}"))
      throw new BusinessException("Informe uma chave de idempotência válida.");
    long usuario = UsuarioAtual.obrigatorio().getId();
    try {
      String hash =
          HexFormat.of()
              .formatHex(
                  MessageDigest.getInstance("SHA-256")
                      .digest(
                          (rota + mapper.writeValueAsString(corpo))
                              .getBytes(StandardCharsets.UTF_8)));
      jdbc.update(
          "INSERT INTO operacoes_idempotentes(usuario_id,chave,conteudo_hash) VALUES(?,?,?) ON"
              + " DUPLICATE KEY UPDATE chave=chave",
          usuario,
          chave,
          hash);
      var row =
          jdbc.queryForMap(
              "SELECT conteudo_hash,resposta FROM operacoes_idempotentes WHERE usuario_id=? AND"
                  + " chave=? FOR UPDATE",
              usuario,
              chave);
      if (!hash.equals(row.get("conteudo_hash")))
        throw new ConflictException("Esta chave já foi usada com outro conteúdo.");
      if (row.get("resposta") != null) return mapper.readValue((String) row.get("resposta"), tipo);
      T resultado = acao.get();
      jdbc.update(
          "UPDATE operacoes_idempotentes SET resposta=? WHERE usuario_id=? AND chave=?",
          mapper.writeValueAsString(resultado),
          usuario,
          chave);
      return resultado;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalStateException("Não foi possível registrar a operação.", e);
    }
  }
}
