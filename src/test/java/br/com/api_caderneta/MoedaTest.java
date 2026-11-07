package br.com.api_caderneta;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.api_caderneta.util.Moeda;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoedaTest {
  @Test
  void apresentaCentavosEMilharNoPadraoBrasileiro() {
    assertEquals("R$ 1,00", Moeda.formatar(new BigDecimal("1")));
    assertEquals("R$ 10,00", Moeda.formatar(new BigDecimal("10.00")));
    assertEquals("R$ 1.234,56", Moeda.formatar(new BigDecimal("1234.56")));
  }
}
