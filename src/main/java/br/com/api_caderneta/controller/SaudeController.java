package br.com.api_caderneta.controller;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
public class SaudeController {
  private final JdbcTemplate jdbc;

  public SaudeController(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @GetMapping("/api/saude")
  public Map<String, String> saude() {
    jdbc.queryForObject("SELECT 1", Integer.class);
    return Map.of("status", "UP");
  }
}
