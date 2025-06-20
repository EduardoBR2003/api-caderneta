package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.FuncionarioDTO;
import br.com.api_caderneta.dto.FuncionarioRequestDTO;
import br.com.api_caderneta.dto.FuncionarioUpdateRequestDTO;
import br.com.api_caderneta.mocks.MockProprietario;
import br.com.api_caderneta.services.ProprietarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProprietarioController.class)
public class ProprietarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProprietarioService service;

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
    void testGetProprietarioById() throws Exception {
        when(service.getProprietarioById(1L)).thenReturn(proprietarioDTO);

        mockMvc.perform(get("/api/v1/proprietarios/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Nome Proprietario 1"));
    }

    @Test
    void testCreateProprietario() throws Exception {
        when(service.createProprietario(any(FuncionarioRequestDTO.class))).thenReturn(proprietarioDTO);

        mockMvc.perform(post("/api/v1/proprietarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proprietarioRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Nome Proprietario 1"));
    }

    @Test
    void testDeleteProprietario() throws Exception {
        mockMvc.perform(delete("/api/v1/proprietarios/1"))
                .andExpect(status().isNoContent());
    }
}