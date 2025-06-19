package br.com.api_caderneta.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class VendaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private LocalDateTime dataHora;
    private BigDecimal valorTotal;
    private Long clienteId;
    private Long funcionarioId;
    private Long dividaId;
    private List<ItemVendaDTO> itens;

    public VendaDTO() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(Long funcionarioId) { this.funcionarioId = funcionarioId; }
    public Long getDividaId() { return dividaId; }
    public void setDividaId(Long dividaId) { this.dividaId = dividaId; }
    public List<ItemVendaDTO> getItens() { return itens; }
    public void setItens(List<ItemVendaDTO> itens) { this.itens = itens; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VendaDTO vendaDTO = (VendaDTO) o;
        return Objects.equals(id, vendaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}