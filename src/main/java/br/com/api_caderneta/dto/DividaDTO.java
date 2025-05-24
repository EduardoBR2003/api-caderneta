package br.com.api_caderneta.dto;

import br.com.api_caderneta.model.enums.StatusDivida; // Corrigido para o pacote correto dos enums
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class DividaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idDivida;
    private LocalDate dataEmissao;
    private LocalDate dataVencimento;
    private BigDecimal valorOriginal;
    private BigDecimal valorPendente;
    private StatusDivida statusDivida;
    private Long clienteId;
    private Long vendaOrigemId;

    public DividaDTO() {
    }

    public DividaDTO(Long idDivida, LocalDate dataEmissao, LocalDate dataVencimento, BigDecimal valorOriginal, BigDecimal valorPendente, StatusDivida statusDivida, Long clienteId, Long vendaOrigemId) {
        this.idDivida = idDivida;
        this.dataEmissao = dataEmissao;
        this.dataVencimento = dataVencimento;
        this.valorOriginal = valorOriginal;
        this.valorPendente = valorPendente;
        this.statusDivida = statusDivida;
        this.clienteId = clienteId;
        this.vendaOrigemId = vendaOrigemId;
    }

    // Getters e Setters
    public Long getIdDivida() {
        return idDivida;
    }

    public void setIdDivida(Long idDivida) {
        this.idDivida = idDivida;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public BigDecimal getValorOriginal() {
        return valorOriginal;
    }

    public void setValorOriginal(BigDecimal valorOriginal) {
        this.valorOriginal = valorOriginal;
    }

    public BigDecimal getValorPendente() {
        return valorPendente;
    }

    public void setValorPendente(BigDecimal valorPendente) {
        this.valorPendente = valorPendente;
    }

    public StatusDivida getStatusDivida() {
        return statusDivida;
    }

    public void setStatusDivida(StatusDivida statusDivida) {
        this.statusDivida = statusDivida;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getVendaOrigemId() {
        return vendaOrigemId;
    }

    public void setVendaOrigemId(Long vendaOrigemId) {
        this.vendaOrigemId = vendaOrigemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DividaDTO dividaDTO = (DividaDTO) o;
        return Objects.equals(idDivida, dividaDTO.idDivida);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDivida);
    }

    @Override
    public String toString() {
        return "DividaDTO{" +
                "idDivida=" + idDivida +
                ", valorOriginal=" + valorOriginal +
                ", valorPendente=" + valorPendente +
                ", statusDivida=" + statusDivida +
                ", clienteId=" + clienteId +
                '}';
    }
}