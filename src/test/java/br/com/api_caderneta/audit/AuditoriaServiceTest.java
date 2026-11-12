package br.com.api_caderneta.audit;

import br.com.api_caderneta.model.AuditoriaEvento;
import br.com.api_caderneta.repository.AuditoriaEventoRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditoriaServiceTest {

    @Test
    void mudancaSoIncluiCamposAlterados() {
        Map<String, Object[]> m = AuditoriaService.mudanca("a", 1, 2, "b", "x", "x", "c", null, 3);
        assertEquals(2, m.size());
        assertArrayEquals(new Object[]{1, 2}, m.get("a"));
        assertArrayEquals(new Object[]{null, 3}, m.get("c"));
        assertFalse(m.containsKey("b"));
    }

    @Test
    void registraAtorExplicitoEJsonAntesDepois() {
        var repo = mock(AuditoriaEventoRepository.class);
        var service = new AuditoriaService(repo, new ObjectMapper());
        service.registrarComo(9L, "cli", "CLIENTE", "ACAO", "USUARIO", 9L, AuditoriaService.mudanca("k", null, "v"));
        var cap = ArgumentCaptor.forClass(AuditoriaEvento.class);
        verify(repo).save(cap.capture());
        var e = cap.getValue();
        assertEquals("SUCESSO", e.getResultado());
        assertEquals("CLIENTE", e.getPerfil());
        assertEquals("{\"k\":{\"antes\":null,\"depois\":\"v\"}}", e.getAlteracoes());
        assertNotNull(e.getOcorridoEm());
    }

    @Test
    void semMudancasNaoGravaJson() {
        var repo = mock(AuditoriaEventoRepository.class);
        new AuditoriaService(repo, new ObjectMapper()).registrarComo(null, null, null, "A", "R", 1L, null);
        var cap = ArgumentCaptor.forClass(AuditoriaEvento.class);
        verify(repo).save(cap.capture());
        assertNull(cap.getValue().getAlteracoes());
    }
}
