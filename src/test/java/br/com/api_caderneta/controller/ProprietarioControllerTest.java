package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.FuncionarioDTO;
import br.com.api_caderneta.dto.FuncionarioRequestDTO;
import br.com.api_caderneta.dto.FuncionarioUpdateRequestDTO;
import br.com.api_caderneta.mocks.MockProprietario;
import br.com.api_caderneta.services.ProprietarioService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import br.com.api_caderneta.security.SecurityConfig;
import br.com.api_caderneta.security.ApiSecurityHandlers;
import br.com.api_caderneta.security.UsuarioDetailsService;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProprietarioController.class)
@Import({SecurityConfig.class, ApiSecurityHandlers.class})
public class ProprietarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProprietarioService service;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockProprietario input;
    private FuncionarioDTO proprietarioDTO;
    private FuncionarioRequestDTO proprietarioRequestDTO;

    @BeforeEach
    void setUp() {
        input = new MockProprietario();
        proprietarioDTO = input.mockDTO(1);
        proprietarioRequestDTO = input.mockRequestDTO(1);
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    void testGetProprietarioById() throws Exception {
        when(service.getProprietarioById(1L)).thenReturn(proprietarioDTO);

        mockMvc.perform(get("/api/v1/proprietarios/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Nome Proprietario 1"));
    }

    /** A criação legada foi fechada: gravava senha fora de usuarios. Use POST /api/auth/funcionarios. */
    @Test
    @WithMockUser(roles = "GERENTE")
    void testCreateProprietarioLegadoEstaFechado() throws Exception {
        mockMvc.perform(post("/api/v1/proprietarios").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proprietarioRequestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    void testDeleteProprietario() throws Exception {
        mockMvc.perform(delete("/api/v1/proprietarios/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void funcionarioNaoAcessaProprietarios() throws Exception {
        mockMvc.perform(get("/api/v1/proprietarios/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACESSO_NEGADO"));
    }

    @Test
    void anonimoRecebeJson401() throws Exception {
        mockMvc.perform(get("/api/v1/proprietarios/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NAO_AUTENTICADO"));
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    void mutacaoSemCsrfEhRecusada() throws Exception {
        mockMvc.perform(delete("/api/v1/proprietarios/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CSRF_INVALID"));
    }
}
