package br.com.api_caderneta;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class VendaDataTest {
  @Test
  void madrugadaUtcPertenceAoDiaAnteriorEmBrasilia() {
    var dto = new br.com.api_caderneta.dto.VendaDTO();
    dto.setDataHora(java.time.LocalDateTime.of(2026, 11, 6, 1, 30));
    assertEquals(java.time.LocalDate.of(2026, 11, 5), dto.getDataNegocio());
  }
}
