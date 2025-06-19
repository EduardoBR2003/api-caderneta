package br.com.api_caderneta.dto;

import br.com.api_caderneta.model.enums.MetodoPagamento;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class PagamentoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private BigDecimal valorPago;
    private LocalDate dataPagamento;
    private MetodoPagamento metodoPagamento;
    private Long dividaId;

    public PagamentoDTO() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
    public MetodoPagamento getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(MetodoPagamento metodoPagamento) { this.metodoPagamento = metodoPagamento; }
    public Long getDividaId() { return dividaId; }
    public void setDividaId(Long dividaId) { this.dividaId = dividaId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PagamentoDTO that = (PagamentoDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}