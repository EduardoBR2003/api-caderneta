package br.com.api_caderneta.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VendaRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // Campos obrigatórios para criação, opcionais para atualização
    private Long clienteId;
    private Long funcionarioId;
    private List<ItemVendaRequestDTO> itens;
    
    // Campos opcionais para atualização
    private LocalDateTime dataHora;
    private BigDecimal valorTotal;

    public VendaRequestDTO() {}

    // Getters e Setters
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(Long funcionarioId) { this.funcionarioId = funcionarioId; }
    public List<ItemVendaRequestDTO> getItens() { return itens; }
    public void setItens(List<ItemVendaRequestDTO> itens) { this.itens = itens; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
}