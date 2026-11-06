package br.com.api_caderneta.historico;

import br.com.api_caderneta.exceptions.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class IntervaloTest {

    @Test
    void aceitaOffsetEConverteParaUtc() {
        var i = Intervalo.de("2026-11-05T00:00:00-03:00", "2026-11-06T00:00:00Z");
        assertEquals(Instant.parse("2026-11-05T03:00:00Z"), i.inicio());
        assertEquals(Instant.parse("2026-11-06T00:00:00Z"), i.fim());
    }

    @Test
    void maisSemCodificarVirouEspacoNaUrl() {
        assertEquals(Instant.parse("2026-11-05T00:00:00Z"), Intervalo.de("2026-11-05T00:00:00 00:00", null).inicio());
    }

    @Test
    void ausenteEhIlimitado() {
        var i = Intervalo.de(null, " ");
        assertNull(i.inicio());
        assertNull(i.fim());
    }

    @Test
    void rejeitaIntervaloInvertidoOuVazio() {
        assertThrows(BusinessException.class, () -> Intervalo.de("2026-11-06T00:00:00Z", "2026-11-05T00:00:00Z"));
        assertThrows(BusinessException.class, () -> Intervalo.de("2026-11-05T00:00:00Z", "2026-11-05T00:00:00Z"));
    }

    @Test
    void rejeitaFormatoSemOffsetOuInvalido() {
        assertThrows(BusinessException.class, () -> Intervalo.de("2026-11-05", null));
        assertThrows(BusinessException.class, () -> Intervalo.de("2026-11-05T00:00:00", null));
        assertThrows(BusinessException.class, () -> Intervalo.de(null, "ontem"));
    }
}
