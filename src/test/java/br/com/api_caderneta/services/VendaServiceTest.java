package br.com.api_caderneta.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.api_caderneta.audit.AuditoriaService;
import br.com.api_caderneta.config.ModelMapperConfig;
import br.com.api_caderneta.dto.*;
import br.com.api_caderneta.exceptions.*;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.*;
import br.com.api_caderneta.model.enums.Perfil;
import br.com.api_caderneta.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VendaServiceTest {
  VendaRepository vendas = mock(VendaRepository.class);
  ClienteRepository clientes = mock(ClienteRepository.class);
  FuncionarioRepository funcionarios = mock(FuncionarioRepository.class);
  DividaRepository dividas = mock(DividaRepository.class);
  NotificacaoService historico = mock(NotificacaoService.class);
  AuditoriaService auditoria = mock(AuditoriaService.class);
  VendaService service =
      new VendaService(
          vendas,
          clientes,
          funcionarios,
          new DataMapper(new ModelMapperConfig().modelMapper()),
          historico,
          auditoria,
          dividas);
  Cliente cliente;
  Funcionario funcionario;

  @BeforeEach
  void inicio() {
    br.com.api_caderneta.TestAuth.entrar(7L, Perfil.FUNCIONARIO);
    cliente = new Cliente();
    cliente.setId(1L);
    cliente.setNome("Cliente");
    cliente.setLimiteCredito(new BigDecimal("1000"));
    cliente.setPrazoPagamentoPadraoDias(30);
    funcionario = new Funcionario();
    funcionario.setId(7L);
    when(clientes.bloquear(1L)).thenReturn(Optional.of(cliente));
    when(funcionarios.findById(7L)).thenReturn(Optional.of(funcionario));
    when(vendas.saveAndFlush(any()))
        .thenAnswer(
            i -> {
              Venda v = i.getArgument(0);
              v.setId(10L);
              v.setVersao(0L);
              v.getDividaGerada().setId(20L);
              return v;
            });
  }

  @AfterEach
  void fim() {
    org.springframework.security.core.context.SecurityContextHolder.clearContext();
  }

  VendaRequestDTO nova(String itens) {
    var req = new VendaRequestDTO();
    req.setClienteId(1L);
    req.setValorTotal(new BigDecimal("100"));
    if (itens != null) {
      var i = new ItemVendaRequestDTO();
      i.setDescricaoProduto("Item");
      i.setQuantidade(1);
      i.setPrecoUnitario(new BigDecimal(itens));
      req.setItens(List.of(i));
    }
    return req;
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "60", "100"})
  void totalIndependenteDeItens(String soma) {
    var req = nova(soma.equals("0") ? null : soma);
    var r = service.createVenda(req);
    assertEquals(0, new BigDecimal("100").compareTo(r.getValorTotal()));
    verify(historico).enviarNotificacao(any(), anyString(), any());
    verify(auditoria).registrar(eq("VENDA_CRIADA"), any(), any(), any());
  }

  @Test
  void itensAcimaDoTotalNaoGravam() {
    assertThrows(BusinessException.class, () -> service.createVenda(nova("100.01")));
    verify(vendas, never()).saveAndFlush(any());
  }

  @Test
  void vazioTambemEhOpcional() {
    var r = nova(null);
    r.setItens(List.of());
    assertEquals(0, new BigDecimal("100").compareTo(service.createVenda(r).getValorTotal()));
  }

  @Test
  void valoresInvalidosNaoGravam() {
    for (String s : List.of("-1", "0", "1.001")) {
      var r = nova(null);
      r.setValorTotal(new BigDecimal(s));
      assertThrows(BusinessException.class, () -> service.createVenda(r));
    }
    var r = nova("1");
    r.getItens().getFirst().setQuantidade(-1);
    assertThrows(BusinessException.class, () -> service.createVenda(r));
    verify(vendas, never()).saveAndFlush(any());
  }

  @Test
  void naoAceitaResponsavelForjado() {
    var r = nova(null);
    r.setFuncionarioId(99L);
    assertThrows(BusinessException.class, () -> service.createVenda(r));
    verify(funcionarios, never()).findById(99L);
  }

  @Test
  void somaCreditoAtualComNovaVenda() {
    when(dividas.somaPendenteDoCliente(eq(1L), any())).thenReturn(new BigDecimal("950"));
    assertThrows(BusinessException.class, () -> service.createVenda(nova(null)));
  }

  @Test
  void clienteInexistente() {
    var r = nova(null);
    r.setClienteId(999L);
    assertThrows(ResourceNotFoundException.class, () -> service.createVenda(r));
  }

  Venda existente(boolean paga) {
    var v = new Venda();
    v.setId(10L);
    v.setVersao(0L);
    v.setCliente(cliente);
    v.setFuncionario(funcionario);
    v.setValorTotal(new BigDecimal("100"));
    var d = new Divida();
    d.setId(20L);
    d.setCliente(cliente);
    d.setValorOriginal(new BigDecimal("100"));
    d.setDataVencimento(br.com.api_caderneta.util.Fuso.hoje().plusDays(30));
    v.setDividaGerada(d);
    if (paga) {
      var p = new Pagamento();
      p.setValorPago(new BigDecimal("40"));
      d.adicionarPagamento(p);
    }
    when(vendas.findById(10L)).thenReturn(Optional.of(v));
    when(vendas.clienteId(10L)).thenReturn(Optional.of(1L));
    when(vendas.bloquear(10L)).thenReturn(Optional.of(v));
    when(dividas.findByIdParaAtualizar(20L)).thenReturn(Optional.of(d));
    return v;
  }

  @Test
  void vendaAlheiaEh404() {
    var v = existente(false);
    v.getFuncionario().setId(8L);
    assertThrows(ResourceNotFoundException.class, () -> service.getVendaById(10L));
  }

  @Test
  void itensAposPagamentoPreservamSaldo() {
    var v = existente(true);
    var r = new VendaUpdateRequestDTO();
    r.setVersao(0L);
    r.setItens(nova("60").getItens());
    service.updateVenda(10L, r);
    assertEquals(new BigDecimal("60"), v.getDividaGerada().getValorPendente());
    assertEquals(new BigDecimal("100"), v.getValorTotal());
  }

  @Test
  void financeiraAposPagamentoEhConflito() {
    existente(true);
    var r = new VendaUpdateRequestDTO();
    r.setVersao(0L);
    r.setValorTotal(new BigDecimal("120"));
    assertThrows(ConflictException.class, () -> service.updateVenda(10L, r));
  }

  @Test
  void versaoAntigaEhConflito() {
    existente(false);
    var r = new VendaUpdateRequestDTO();
    r.setVersao(9L);
    assertThrows(ConflictException.class, () -> service.updateVenda(10L, r));
  }

  @Test
  void edicaoRevalidaCredito() {
    existente(false);
    var r = new VendaUpdateRequestDTO();
    r.setVersao(0L);
    r.setValorTotal(new BigDecimal("2000"));
    assertThrows(BusinessException.class, () -> service.updateVenda(10L, r));
  }

  @Test
  void corrigirValorPreservaPrazoRenegociado() {
    var venda = existente(false);
    var vencimento = br.com.api_caderneta.util.Fuso.hoje().plusDays(90);
    venda.getDividaGerada().setDataVencimento(vencimento);
    var pedido = new VendaUpdateRequestDTO();
    pedido.setVersao(0L);
    pedido.setValorTotal(new BigDecimal("110"));
    service.updateVenda(10L, pedido);
    assertEquals(vencimento, venda.getDividaGerada().getDataVencimento());
  }

  @Test
  void excluirComPagamentoEhConflito() {
    br.com.api_caderneta.TestAuth.entrar(7L, Perfil.GERENTE);
    existente(true);
    assertThrows(ConflictException.class, () -> service.deleteVenda(10L));
    verify(vendas, never()).delete(any(Venda.class));
  }
}
