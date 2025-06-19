package br.com.api_caderneta.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

public class VendaRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private Long clienteId;

    @NotNull
    private Long funcionarioId;

    @NotEmpty @Valid
    private List<ItemVendaRequestDTO> itens;

    public VendaRequestDTO() {}

    // Getters e Setters
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(Long funcionarioId) { this.funcionarioId = funcionarioId; }
    public List<ItemVendaRequestDTO> getItens() { return itens; }
    public void setItens(List<ItemVendaRequestDTO> itens) { this.itens = itens; }
}