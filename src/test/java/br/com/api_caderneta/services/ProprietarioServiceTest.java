package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.FuncionarioDTO;
import br.com.api_caderneta.dto.FuncionarioRequestDTO;
import br.com.api_caderneta.dto.FuncionarioUpdateRequestDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.mocks.MockProprietario;
import br.com.api_caderneta.model.Proprietario;
import br.com.api_caderneta.repository.FuncionarioRepository;
import br.com.api_caderneta.repository.ProprietarioRepository;
import br.com.api_caderneta.repository.VendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProprietarioServiceTest {

    @Mock
    private ProprietarioRepository repository;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private VendaRepository vendaRepository;
    @Mock
    private DataMapper mapper;

    @InjectMocks
    private ProprietarioService service;

    private MockProprietario input;

    @BeforeEach
    void setUp() {
        input = new MockProprietario();
    }

    @Test
    void testCreateProprietario_Success() {
        FuncionarioRequestDTO requestDTO = input.mockRequestDTO();
        Proprietario entity = input.mockEntity();
        FuncionarioDTO dto = input.mockDTO();

        when(funcionarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(funcionarioRepository.existsByLogin(anyString())).thenReturn(false);
        when(mapper.parseObject(requestDTO, Proprietario.class)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.parseObject(entity, FuncionarioDTO.class)).thenReturn(dto);

        var result = service.createProprietario(requestDTO);
        assertNotNull(result);
        assertEquals(dto.getId(), result.getId());
    }

    @Test
    void testCreateProprietario_CpfExists() {
        FuncionarioRequestDTO requestDTO = input.mockRequestDTO();
        when(funcionarioRepository.existsByCpf(anyString())).thenReturn(true);

        Exception exception = assertThrows(BusinessException.class, () -> service.createProprietario(requestDTO));
        String expectedMessage = "Já existe uma pessoa (funcionário ou cliente) cadastrada com o CPF informado.";
        assertTrue(exception.getMessage().contains(expectedMessage));

        verify(repository, never()).save(any());
    }

    @Test
    void testDeleteProprietario_HasSales() {
        Long proprietarioId = 1L;
        Proprietario entity = input.mockEntity(1);

        when(repository.findById(proprietarioId)).thenReturn(Optional.of(entity));
        when(vendaRepository.existsByFuncionarioId(proprietarioId)).thenReturn(true);

        Exception exception = assertThrows(BusinessException.class, () -> service.deleteProprietario(proprietarioId));

        String expectedMessage = "Não é possível excluir um proprietário que já realizou vendas.";
        assertEquals(expectedMessage, exception.getMessage());

        verify(repository, never()).delete(any());
    }
}