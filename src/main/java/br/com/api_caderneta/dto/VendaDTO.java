package br.com.api_caderneta.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class VendaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idVenda;
    private LocalDateTime dataHora;
    private BigDecimal valorTotal;
    private Long clienteId;
    private Long funcionarioId;
    private List<ItemVendaDTO> itensVenda;
    private Long dividaGeradaId;

    public VendaDTO() {
    }

    public VendaDTO(Long idVenda, LocalDateTime dataHora, BigDecimal valorTotal, Long clienteId, Long funcionarioId, List<ItemVendaDTO> itensVenda, Long dividaGeradaId) {
        this.idVenda = idVenda;
        this.dataHora = dataHora;
        this.valorTotal = valorTotal;
        this.clienteId = clienteId;
        this.funcionarioId = funcionarioId;
        this.itensVenda = itensVenda;
        this.dividaGeradaId = dividaGeradaId;
    }

    // Getters e Setters
    public Long getIdVenda() {
        return idVenda;
    }

    public void setIdVenda(Long idVenda) {
        this.idVenda = idVenda;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getFuncionarioId() {
        return funcionarioId;
    }

    public void setFuncionarioId(Long funcionarioId) {
        this.funcionarioId = funcionarioId;
    }

    public List<ItemVendaDTO> getItensVenda() {
        return itensVenda;
    }

    public void setItensVenda(List<ItemVendaDTO> itensVenda) {
        this.itensVenda = itensVenda;
    }

    public Long getDividaGeradaId() {
        return dividaGeradaId;
    }

    public void setDividaGeradaId(Long dividaGeradaId) {
        this.dividaGeradaId = dividaGeradaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VendaDTO vendaDTO = (VendaDTO) o;
        return Objects.equals(idVenda, vendaDTO.idVenda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idVenda);
    }

    @Override
    public String toString() {
        return "VendaDTO{" +
                "idVenda=" + idVenda +
                ", dataHora=" + dataHora +
                ", valorTotal=" + valorTotal +
                ", clienteId=" + clienteId +
                '}';
    }
}