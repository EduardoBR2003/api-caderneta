package br.com.api_caderneta.dto;

import br.com.api_caderneta.model.enums.MetodoPagamento;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

public class PagamentoRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private BigDecimal valorPago;

    @NotNull
    private MetodoPagamento metodoPagamento;

    public PagamentoRequestDTO() {}

    // Getters e Setters
    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }
    public MetodoPagamento getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(MetodoPagamento metodoPagamento) { this.metodoPagamento = metodoPagamento; }
}