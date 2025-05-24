package br.com.api_caderneta.dto;

import br.com.api_caderneta.model.enums.MetodoPagamento; // Corrigido para o pacote correto dos enums
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class PagamentoRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long dividaId;
    private BigDecimal valorPago;
    private MetodoPagamento metodoPagamento;
    private LocalDate dataPagamento; // Opcional, pode ser preenchido pelo servidor

    public PagamentoRequestDTO() {
    }

    public PagamentoRequestDTO(Long dividaId, BigDecimal valorPago, MetodoPagamento metodoPagamento, LocalDate dataPagamento) {
        this.dividaId = dividaId;
        this.valorPago = valorPago;
        this.metodoPagamento = metodoPagamento;
        this.dataPagamento = dataPagamento;
    }

    // Getters e Setters
    public Long getDividaId() {
        return dividaId;
    }

    public void setDividaId(Long dividaId) {
        this.dividaId = dividaId;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public void setValorPago(BigDecimal valorPago) {
        this.valorPago = valorPago;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PagamentoRequestDTO that = (PagamentoRequestDTO) o;
        return Objects.equals(dividaId, that.dividaId) &&
                Objects.equals(valorPago, that.valorPago) &&
                metodoPagamento == that.metodoPagamento &&
                Objects.equals(dataPagamento, that.dataPagamento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dividaId, valorPago, metodoPagamento, dataPagamento);
    }

    @Override
    public String toString() {
        return "PagamentoRequestDTO{" +
                "dividaId=" + dividaId +
                ", valorPago=" + valorPago +
                ", metodoPagamento=" + metodoPagamento +
                '}';
    }
}