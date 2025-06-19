package br.com.api_caderneta.mocks;

import br.com.api_caderneta.model.Cliente;
import br.com.api_caderneta.model.Divida;
import br.com.api_caderneta.model.Venda;
import br.com.api_caderneta.model.enums.StatusDivida;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MockDivida {

    public Divida mockEntity(Cliente cliente, Venda venda) {
        Divida divida = new Divida();
        divida.setId(1L);
        divida.setCliente(cliente);
        divida.setVendaOrigem(venda);
        divida.setValorOriginal(new BigDecimal("150.00"));
        divida.setValorPendente(new BigDecimal("150.00"));
        divida.setDataEmissao(LocalDate.now());
        divida.setDataVencimento(LocalDate.now().plusDays(30));
        divida.setStatusDivida(StatusDivida.ABERTA);
        return divida;
    }
}