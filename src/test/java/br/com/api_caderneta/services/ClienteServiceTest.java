package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.ClienteDTO;
import br.com.api_caderneta.dto.ClienteRequestDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.mocks.MockCliente;
import br.com.api_caderneta.model.Cliente;
import br.com.api_caderneta.repository.ClienteRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Garante a inicialização correta dos mocks
class ClienteServiceTest {

    MockCliente input;

    @InjectMocks
    private ClienteService service;

    @Mock
    private ClienteRepository repository;

    @Mock
    private DataMapper mapper;

    @BeforeEach
    void setUp() {
        input = new MockCliente();
        // A linha MockitoAnnotations.openMocks(this) foi removida,
        // pois @ExtendWith(MockitoExtension.class) já faz esse trabalho.
    }

    @Test
    void testGetClienteById_Success() {
        // Arrange
        Cliente entity = input.mockEntity(1L);
        ClienteDTO dto = input.mockDTO(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.parseObject(entity, ClienteDTO.class)).thenReturn(dto);

        // Act
        var result = service.getClienteById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Nome Teste1", result.getNome());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void testGetClienteById_NotFound() {
        // Arrange
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            service.getClienteById(1L);
        });

        String expectedMessage = "Cliente não encontrado com o ID: 1";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }


    @Test
    void testCreateCliente_Success() {
        // Arrange
        ClienteRequestDTO requestDto = input.mockRequestDTO();
        Cliente entityToSave = input.mockEntity(); // Entidade antes de salvar (sem ID)
        Cliente savedEntity = input.mockEntity(1L); // Entidade como retornada do repo (com ID)
        ClienteDTO resultDto = input.mockDTO(1L); // DTO esperado no final

        when(repository.existsByCpf(requestDto.getCpf())).thenReturn(false);
        when(mapper.parseObject(requestDto, Cliente.class)).thenReturn(entityToSave);
        when(repository.save(entityToSave)).thenReturn(savedEntity);
        when(mapper.parseObject(savedEntity, ClienteDTO.class)).thenReturn(resultDto);


        // Act
        var result = service.createCliente(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository, times(1)).save(entityToSave);
    }

    @Test
    void testCreateCliente_CpfExists() {
        // Arrange
        ClienteRequestDTO dto = input.mockRequestDTO();
        when(repository.existsByCpf(dto.getCpf())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(BusinessException.class, () -> {
            service.createCliente(dto);
        });

        String expectedMessage = "Já existe um cliente cadastrado com o CPF informado.";
        assertEquals(expectedMessage, exception.getMessage());

        verify(repository, never()).save(any(Cliente.class));
    }

    @Test
    void testDeleteCliente_Success() {
        // Arrange
        Cliente entity = input.mockEntity(1L);
        entity.setDividas(Collections.emptyList()); // Garante que não há dívidas
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        // O método não tem retorno, então não precisa de `doNothing` se não lançar exceção.

        // Act
        service.deleteCliente(1L);

        // Assert
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).delete(entity);
    }

    @Test
    void testDeleteCliente_WithDebts() {
        // Arrange
        Cliente entity = input.mockEntity(1L);
        // Simula que o cliente tem dívidas (a lista não está vazia)
        entity.setDividas(Collections.singletonList(new br.com.api_caderneta.model.Divida()));
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        // Act & Assert
        Exception exception = assertThrows(BusinessException.class, () -> {
            service.deleteCliente(1L);
        });

        String expectedMessage = "Não é possível excluir um cliente que possui dívidas pendentes.";
        assertEquals(expectedMessage, exception.getMessage());

        verify(repository, never()).delete(any(Cliente.class));
    }
}