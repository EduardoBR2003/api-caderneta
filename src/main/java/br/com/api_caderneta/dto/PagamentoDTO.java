package br.com.api_caderneta.dto;

import br.com.api_caderneta.model.enums.MetodoPagamento; // Corrigido para o pacote correto dos enums
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class PagamentoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idPagamento;
    private LocalDate dataPagamento;
    private BigDecimal valorPago;
    private MetodoPagamento metodoPagamento;
    private Long dividaId;

    public PagamentoDTO() {
    }

    public PagamentoDTO(Long idPagamento, LocalDate dataPagamento, BigDecimal valorPago, MetodoPagamento metodoPagamento, Long dividaId) {
        this.idPagamento = idPagamento;
        this.dataPagamento = dataPagamento;
        this.valorPago = valorPago;
        this.metodoPagamento = metodoPagamento;
        this.dividaId = dividaId;
    }

    // Getters e Setters
    public Long getIdPagamento() {
        return idPagamento;
    }

    public void setIdPagamento(Long idPagamento) {
        this.idPagamento = idPagamento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
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

    public Long getDividaId() {
        return dividaId;
    }

    public void setDividaId(Long dividaId) {
        this.dividaId = dividaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PagamentoDTO that = (PagamentoDTO) o;
        return Objects.equals(idPagamento, that.idPagamento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPagamento);
    }

    @Override
    public String toString() {
        return "PagamentoDTO{" +
                "idPagamento=" + idPagamento +
                ", dataPagamento=" + dataPagamento +
                ", valorPago=" + valorPago +
                ", dividaId=" + dividaId +
                '}';
    }
}