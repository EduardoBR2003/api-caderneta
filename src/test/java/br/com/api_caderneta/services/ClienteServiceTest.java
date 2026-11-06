package br.com.api_caderneta.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import br.com.api_caderneta.dto.ClienteDTO;
import br.com.api_caderneta.dto.ClienteRequestDTO;
import br.com.api_caderneta.dto.ClienteResponseDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.mocks.MockCliente;
import br.com.api_caderneta.model.Cliente;
import br.com.api_caderneta.repository.ClienteRepository;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class) // Garante a inicialização correta dos mocks
class ClienteServiceTest {

  MockCliente input;

  @InjectMocks private ClienteService service;

  @Mock private ClienteRepository repository;

  @Mock private DataMapper mapper;
  @Mock private NotificacaoService notificacao;
  @Mock private br.com.api_caderneta.audit.AuditoriaService auditoria;

  @BeforeEach
  void setUp() {
    input = new MockCliente();
    org.springframework.test.util.ReflectionTestUtils.setField(service, "auditoria", auditoria);
    br.com.api_caderneta.TestAuth.entrar(1L, br.com.api_caderneta.model.enums.Perfil.GERENTE);
    // A linha MockitoAnnotations.openMocks(this) foi removida,
    // pois @ExtendWith(MockitoExtension.class) já faz esse trabalho.
  }

  @Test
  void testGetClienteById_Success() {
    // Arrange
    Cliente entity = input.mockEntity(1L);
    ClienteResponseDTO dto = new ClienteResponseDTO();
    dto.setId(1L);
    dto.setNome("Nome Teste1");

    when(repository.findById(1L)).thenReturn(Optional.of(entity));
    when(mapper.parseObject(entity, ClienteResponseDTO.class)).thenReturn(dto);

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
    Exception exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> {
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
    when(repository.saveAndFlush(entityToSave)).thenReturn(savedEntity);
    when(mapper.parseObject(savedEntity, ClienteDTO.class)).thenReturn(resultDto);

    // Act
    var result = service.createCliente(requestDto);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(repository, times(1)).saveAndFlush(entityToSave);
  }

  @Test
  void testCreateCliente_CpfExists() {
    // Arrange
    ClienteRequestDTO dto = input.mockRequestDTO();
    when(repository.existsByCpf(dto.getCpf())).thenReturn(true);

    // Act & Assert
    Exception exception =
        assertThrows(
            BusinessException.class,
            () -> {
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
    when(repository.bloquear(1L)).thenReturn(Optional.of(entity));
    // O método não tem retorno, então não precisa de `doNothing` se não lançar exceção.

    // Act
    service.deleteCliente(1L);

    // Assert
    verify(repository, times(1)).bloquear(1L);
    verify(repository, times(1)).delete(entity);
  }

  @Test
  void testDeleteCliente_WithDebts() {
    // Arrange
    Cliente entity = input.mockEntity(1L);
    // Simula que o cliente tem dívidas (a lista não está vazia)
    entity.setDividas(Collections.singletonList(new br.com.api_caderneta.model.Divida()));
    when(repository.bloquear(1L)).thenReturn(Optional.of(entity));

    // Act & Assert
    Exception exception =
        assertThrows(
            BusinessException.class,
            () -> {
              service.deleteCliente(1L);
            });

    String expectedMessage = "Não é possível excluir um cliente que possui dívidas pendentes.";
    assertEquals(expectedMessage, exception.getMessage());

    verify(repository, never()).delete(any(Cliente.class));
  }
}
