package br.com.api_caderneta.services;

import static org.junit.jupiter.api.Assertions.*;

import br.com.api_caderneta.dto.ItemVendaRequestDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.model.Venda;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ItensVendaTest {
  private ItemVendaRequestDTO item(int quantidade, String preco) {
    var item = new ItemVendaRequestDTO();
    item.setDescricaoProduto("  Arroz  ");
    item.setQuantidade(quantidade);
    item.setPrecoUnitario(new BigDecimal(preco));
    return item;
  }

  @Test
  void totalPodeTerParteNaoDetalhadaOuNenhumItem() {
    assertEquals(0, ItensVenda.validar(null, new BigDecimal("100")).signum());
    assertEquals(
        new BigDecimal("50.00"),
        ItensVenda.validar(List.of(item(2, "25.00")), new BigDecimal("100")));
    assertThrows(
        BusinessException.class,
        () -> ItensVenda.validar(List.of(item(2, "25.00")), new BigDecimal("49.99")));
  }

  @Test
  void recusaQuantidadeEPrecisaoAntesDeAlterarVenda() {
    assertThrows(
        BusinessException.class,
        () -> ItensVenda.validar(List.of(item(0, "25")), new BigDecimal("100")));
    assertThrows(
        BusinessException.class,
        () -> ItensVenda.validar(List.of(item(1, "25.001")), new BigDecimal("100")));
  }

  @Test
  void detalhamentoMantemVinculoComVendaESubstituiItensAnteriores() {
    var venda = new Venda();
    ItensVenda.aplicar(venda, List.of(item(2, "25")));
    var salvo = venda.getItensVenda().getFirst();
    assertSame(venda, salvo.getVenda());
    assertEquals("Arroz", salvo.getDescricaoProduto());
    assertEquals(new BigDecimal("25.00"), salvo.getPrecoUnitario());
    ItensVenda.aplicar(venda, List.of());
    assertTrue(venda.getItensVenda().isEmpty());
  }
}
