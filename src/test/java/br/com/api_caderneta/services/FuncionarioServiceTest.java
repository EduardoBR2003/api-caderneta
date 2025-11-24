package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.FuncionarioDTO;
import br.com.api_caderneta.dto.FuncionarioRequestDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.mocks.MockFuncionario;
import br.com.api_caderneta.model.Funcionario;
import br.com.api_caderneta.repository.FuncionarioRepository;
import br.com.api_caderneta.repository.VendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    MockFuncionario input;

    @InjectMocks
    private FuncionarioService service;

    @Mock
    private FuncionarioRepository repository;

    @Mock
    private VendaRepository vendaRepository;

    @Mock
    private DataMapper mapper;


    @BeforeEach
    void setUp() {
        input = new MockFuncionario();
        // Inicialização duplicada do Mockito foi removida
    }

    @Test
    void testCreateFuncionario_Success() {
        // Arrange
        FuncionarioRequestDTO requestDto = input.mockRequestDTO();
        Funcionario entityToSave = input.mockEntity();
        Funcionario savedEntity = input.mockEntity(1L);
        FuncionarioDTO resultDto = input.mockDTO(1L);

        // Configurando todos os mocks necessários
        when(repository.existsByCpf(anyString())).thenReturn(false);
        when(mapper.parseObject(requestDto, Funcionario.class)).thenReturn(entityToSave);
        when(repository.save(entityToSave)).thenReturn(savedEntity);
        when(mapper.parseObject(savedEntity, FuncionarioDTO.class)).thenReturn(resultDto);

        // Act
        FuncionarioDTO result = service.createFuncionario(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository, times(1)).save(entityToSave);
    }

    @Test
    void testCreateFuncionario_CpfExists() {
        // Arrange
        FuncionarioRequestDTO dto = input.mockRequestDTO();
        when(repository.existsByCpf(dto.getCpf())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(BusinessException.class, () -> service.createFuncionario(dto));
        assertEquals("Já existe um funcionário cadastrado com o CPF informado.", exception.getMessage());

        verify(repository, never()).save(any(Funcionario.class));
    }



    @Test
    void testDeleteFuncionario_Success_NoSales() {
        // Arrange
        Funcionario entity = input.mockEntity(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(vendaRepository.existsByFuncionarioId(1L)).thenReturn(false);

        // Act
        service.deleteFuncionario(1L);

        // Assert
        verify(repository, times(1)).findById(1L);
        verify(vendaRepository, times(1)).existsByFuncionarioId(1L);
        verify(repository, times(1)).delete(entity);
    }

    @Test
    void testDeleteFuncionario_WithSales() {
        // Arrange
        Funcionario entity = input.mockEntity(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(vendaRepository.existsByFuncionarioId(1L)).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(BusinessException.class, () -> service.deleteFuncionario(1L));
        assertEquals("Não é possível excluir um funcionário que já realizou vendas.", exception.getMessage());

        verify(repository, never()).delete(any(Funcionario.class));
    }

    @Test
    void testDeleteFuncionario_NotFound() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> service.deleteFuncionario(1L));
    }
}