package br.com.api_caderneta.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.api_caderneta.dto.DividaDTO;
import br.com.api_caderneta.dto.PagamentoRequestDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.mocks.MockCliente;
import br.com.api_caderneta.mocks.MockDivida;
import br.com.api_caderneta.mocks.MockFuncionario;
import br.com.api_caderneta.mocks.MockVenda;
import br.com.api_caderneta.model.Divida;
import br.com.api_caderneta.model.Pagamento;
import br.com.api_caderneta.model.enums.MetodoPagamento;
import br.com.api_caderneta.model.enums.StatusDivida;
import br.com.api_caderneta.repository.DividaRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DividaServiceTest {

  MockDivida input;

  @InjectMocks private DividaService service;

  @Mock private DividaRepository repository;

  @Mock private DataMapper mapper;
  @Mock private br.com.api_caderneta.repository.ClienteRepository clientes;
  @Mock private NotificacaoService notificacao;

  @Mock private br.com.api_caderneta.audit.AuditoriaService auditoria;

  @BeforeEach
  void setUp() {
    input = new MockDivida();
    org.springframework.test.util.ReflectionTestUtils.setField(service, "clientes", clientes);
    br.com.api_caderneta.TestAuth.entrar(1L, br.com.api_caderneta.model.enums.Perfil.GERENTE);
    lenient().when(repository.clienteId(1L)).thenReturn(Optional.of(1L));
    lenient().when(clientes.bloquear(1L)).thenReturn(Optional.of(new MockCliente().mockEntity(1L)));
  }

  private Divida setupMockDivida() {
    var cliente = new MockCliente().mockEntity(1L);
    var funcionario = new MockFuncionario().mockEntity(1L);
    var venda = new MockVenda().mockEntity(cliente, funcionario);
    Divida divida = input.mockEntity(cliente, venda);
    divida.setId(1L);
    return divida;
  }

  @Test
  void testRegistrarPagamento_Success_PagamentoParcial() {
    // Arrange
    Divida divida = setupMockDivida();
    PagamentoRequestDTO requestDTO = new PagamentoRequestDTO();
    requestDTO.setValorPago(new BigDecimal("100.00"));
    requestDTO.setMetodoPagamento(MetodoPagamento.PIX);

    // **CORREÇÃO APLICADA AQUI**
    // Cria uma entidade Pagamento com o valor correto para ser usada pelo mock.
    Pagamento pagamentoEntity = new Pagamento();
    pagamentoEntity.setValorPago(requestDTO.getValorPago());
    pagamentoEntity.setMetodoPagamento(requestDTO.getMetodoPagamento());

    when(mapper.parseObject(any(PagamentoRequestDTO.class), eq(Pagamento.class)))
        .thenReturn(pagamentoEntity);
    when(mapper.parseObject(any(Divida.class), eq(DividaDTO.class))).thenReturn(new DividaDTO());

    lenient().when(repository.findByIdParaAtualizar(1L)).thenReturn(Optional.of(divida));
    when(repository.save(any(Divida.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    service.registrarPagamento(1L, requestDTO);

    // Assert
    verify(repository, times(1)).save(divida);
    assertEquals(
        0,
        new BigDecimal("50.00").compareTo(divida.getValorPendente()),
        "O valor pendente deve ser 50.00");
    assertEquals(StatusDivida.PAGA_PARCIALMENTE, divida.getStatusDivida());
    assertEquals(1, divida.getPagamentos().size());
  }

  @Test
  void testRegistrarPagamento_Success_PagamentoTotal() {
    // Arrange
    Divida divida = setupMockDivida();
    PagamentoRequestDTO requestDTO = new PagamentoRequestDTO();
    requestDTO.setValorPago(new BigDecimal("150.00"));
    requestDTO.setMetodoPagamento(MetodoPagamento.DINHEIRO);

    // **CORREÇÃO APLICADA AQUI**
    Pagamento pagamentoEntity = new Pagamento();
    pagamentoEntity.setValorPago(requestDTO.getValorPago());
    pagamentoEntity.setMetodoPagamento(requestDTO.getMetodoPagamento());

    when(mapper.parseObject(any(PagamentoRequestDTO.class), eq(Pagamento.class)))
        .thenReturn(pagamentoEntity);
    when(mapper.parseObject(any(Divida.class), eq(DividaDTO.class))).thenReturn(new DividaDTO());

    lenient().when(repository.findByIdParaAtualizar(1L)).thenReturn(Optional.of(divida));
    when(repository.save(any(Divida.class))).thenReturn(divida);

    // Act
    service.registrarPagamento(1L, requestDTO);

    // Assert
    verify(repository, times(1)).save(divida);
    assertEquals(
        0, divida.getValorPendente().compareTo(BigDecimal.ZERO), "O valor pendente deve ser 0");
    assertEquals(StatusDivida.PAGA_TOTALMENTE, divida.getStatusDivida());
  }

  @Test
  void testRegistrarPagamento_DividaNotFound() {
    // Arrange
    PagamentoRequestDTO requestDTO = new PagamentoRequestDTO();
    requestDTO.setValorPago(new BigDecimal("100.00"));
    when(repository.clienteId(1L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          service.registrarPagamento(1L, requestDTO);
        });
  }

  @Test
  void testRegistrarPagamento_ValorMaiorQuePendente() {
    // Arrange
    Divida divida = setupMockDivida();
    PagamentoRequestDTO requestDTO = new PagamentoRequestDTO();
    requestDTO.setValorPago(new BigDecimal("200.00"));
    requestDTO.setMetodoPagamento(MetodoPagamento.PIX);

    when(repository.findByIdParaAtualizar(1L)).thenReturn(Optional.of(divida));

    // Act & Assert
    Exception exception =
        assertThrows(
            BusinessException.class,
            () -> {
              service.registrarPagamento(1L, requestDTO);
            });

    assertEquals(
        "O valor do pagamento não pode ser maior que o valor pendente.", exception.getMessage());
    verify(repository, never()).save(any(Divida.class));
  }

  @Test
  void testRegistrarPagamento_ValorZeroOuNegativo() {
    // Arrange
    Divida divida = setupMockDivida();
    PagamentoRequestDTO requestDTO = new PagamentoRequestDTO();
    requestDTO.setValorPago(BigDecimal.ZERO);

    // Act & Assert
    Exception exception =
        assertThrows(
            BusinessException.class,
            () -> {
              service.registrarPagamento(1L, requestDTO);
            });

    assertEquals("Informe um valor maior que zero.", exception.getMessage());
    verify(repository, never()).save(any(Divida.class));
  }

  @Test
  void testUpdateDivida_AlteraSomenteOVencimentoEAudita() {
    Divida divida = setupMockDivida();
    var antes = divida.getDataVencimento();
    var novo = divida.getDataEmissao().plusDays(90);
    var dto = new DividaDTO();
    dto.setDataVencimento(novo);
    dto.setValorPendente(new BigDecimal("1.00")); // deve ser ignorado
    var valorPendente = divida.getValorPendente();

    when(repository.findByIdParaAtualizar(1L)).thenReturn(Optional.of(divida));
    when(repository.save(any(Divida.class))).thenAnswer(i -> i.getArgument(0));

    service.updateDivida(1L, dto);

    assertEquals(novo, divida.getDataVencimento());
    assertNotEquals(antes, divida.getDataVencimento());
    assertEquals(0, valorPendente.compareTo(divida.getValorPendente()));
    verify(auditoria)
        .registrar(
            eq("DIVIDA_EDITADA"),
            eq("DIVIDA"),
            eq(1L),
            argThat(m -> m.containsKey("dataVencimento")));
  }

  @Test
  void testUpdateDivida_SemVencimentoOuQuitadaEhRecusada() {
    Divida divida = setupMockDivida();
    when(repository.findByIdParaAtualizar(1L)).thenReturn(Optional.of(divida));
    assertThrows(BusinessException.class, () -> service.updateDivida(1L, new DividaDTO()));

    divida.setStatusDivida(StatusDivida.PAGA_TOTALMENTE);
    var dto = new DividaDTO();
    dto.setDataVencimento(divida.getDataEmissao().plusDays(10));
    assertThrows(
        br.com.api_caderneta.exceptions.ConflictException.class,
        () -> service.updateDivida(1L, dto));
    verify(repository, never()).save(any(Divida.class));
  }
}
