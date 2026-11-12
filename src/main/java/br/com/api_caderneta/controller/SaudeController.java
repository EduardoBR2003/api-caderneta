package br.com.api_caderneta.controller;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

/** Sondas públicas, sem detalhes internos. */
@RestController
public class SaudeController {
  private final JdbcTemplate jdbc;

  public SaudeController(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /** Prontidão: a API responde e alcança o banco (startup probe / verificação de disponibilidade). */
  @GetMapping("/api/saude")
  public Map<String, String> saude() {
    jdbc.queryForObject("SELECT 1", Integer.class);
    return Map.of("status", "UP");
  }

  /** Vivacidade: só o processo. Não depende do banco, para uma oscilação dele não reiniciar a API. */
  @GetMapping("/api/saude/vivo")
  public Map<String, String> vivo() {
    return Map.of("status", "UP");
  }
}
