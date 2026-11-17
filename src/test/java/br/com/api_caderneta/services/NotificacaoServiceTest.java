package br.com.api_caderneta.services;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.api_caderneta.model.*;
import br.com.api_caderneta.repository.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class NotificacaoServiceTest {
  @Test
  void lembreteTemChaveDeNegocioEInsertAtomico() {
    var n = mock(NotificacaoRepository.class);
    var d = mock(DividaRepository.class);
    var jdbc = mock(JdbcTemplate.class);
    var s = new LembretesVencimento(d, n, jdbc);
    var cliente = new Cliente();
    cliente.setId(1L);
    var divida = new Divida();
    divida.setCliente(cliente);
    divida.setId(7L);
    ReflectionTestUtils.setField(divida, "lojaId", 3L);
    divida.setValorOriginal(new java.math.BigDecimal("100"));
    var ontem = br.com.api_caderneta.util.Fuso.hoje().minusDays(1);
    divida.setDataVencimento(ontem);
    when(d.findComSaldoVencendoAntes(any(), any())).thenReturn(List.of(divida));
    s.executar();
    s.executar();
    verify(jdbc, times(2))
        .update(
            contains("INSERT IGNORE"),
            eq(3L),
            eq(1L),
            contains("venceu"),
            any(),
            eq("divida:7:venceu:" + ontem));
  }
}
