package br.com.api_caderneta.mocks;

import br.com.api_caderneta.dto.ItemVendaRequestDTO;
import br.com.api_caderneta.dto.VendaRequestDTO;
import br.com.api_caderneta.model.Cliente;
import br.com.api_caderneta.model.Funcionario;
import br.com.api_caderneta.model.ItemVenda;
import br.com.api_caderneta.model.Venda;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MockVenda {

    public Venda mockEntity(Cliente cliente, Funcionario funcionario) {
        Venda venda = new Venda();
        venda.setId(1L);
        venda.setCliente(cliente);
        venda.setFuncionario(funcionario);

        ItemVenda item1 = new ItemVenda();
        item1.setId(1L);
        item1.setDescricaoProduto("Produto A");
        item1.setQuantidade(1);
        item1.setPrecoUnitario(new BigDecimal("100.00"));
        venda.adicionarItemVenda(item1);

        ItemVenda item2 = new ItemVenda();
        item2.setId(2L);
        item2.setDescricaoProduto("Produto B");
        item2.setQuantidade(1);
        item2.setPrecoUnitario(new BigDecimal("50.00"));
        venda.adicionarItemVenda(item2);

        venda.setValorTotal(venda.calcularValorTotal());
        return venda;
    }

    public VendaRequestDTO mockRequestDTO(Long clienteId, Long funcionarioId) {
        VendaRequestDTO dto = new VendaRequestDTO();
        dto.setClienteId(clienteId);
        dto.setFuncionarioId(funcionarioId);

        List<ItemVendaRequestDTO> itens = new ArrayList<>();
        ItemVendaRequestDTO item1 = new ItemVendaRequestDTO();
        item1.setDescricaoProduto("Produto A");
        item1.setQuantidade(1);
        item1.setPrecoUnitario(new BigDecimal("100.00"));
        itens.add(item1);

        ItemVendaRequestDTO item2 = new ItemVendaRequestDTO();
        item2.setDescricaoProduto("Produto B");
        item2.setQuantidade(1);
        item2.setPrecoUnitario(new BigDecimal("50.00"));
        itens.add(item2);

        dto.setItens(itens);
        return dto;
    }
}