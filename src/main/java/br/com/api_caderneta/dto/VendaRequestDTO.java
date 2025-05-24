package br.com.api_caderneta.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class VendaRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long clienteId;
    private Long funcionarioId; // Pode ser inferido pelo usuário logado no serviço
    private List<ItemVendaRequestDTO> itensVenda;

    public VendaRequestDTO() {
    }

    public VendaRequestDTO(Long clienteId, Long funcionarioId, List<ItemVendaRequestDTO> itensVenda) {
        this.clienteId = clienteId;
        this.funcionarioId = funcionarioId;
        this.itensVenda = itensVenda;
    }

    // Getters e Setters
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

    public List<ItemVendaRequestDTO> getItensVenda() {
        return itensVenda;
    }

    public void setItensVenda(List<ItemVendaRequestDTO> itensVenda) {
        this.itensVenda = itensVenda;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VendaRequestDTO that = (VendaRequestDTO) o;
        return Objects.equals(clienteId, that.clienteId) &&
                Objects.equals(funcionarioId, that.funcionarioId) &&
                Objects.equals(itensVenda, that.itensVenda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clienteId, funcionarioId, itensVenda);
    }

    @Override
    public String toString() {
        return "VendaRequestDTO{" +
                "clienteId=" + clienteId +
                ", funcionarioId=" + funcionarioId +
                ", numeroItens=" + (itensVenda != null ? itensVenda.size() : 0) +
                '}';
    }
}