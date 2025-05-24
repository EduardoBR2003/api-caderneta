package br.com.api_caderneta.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class FiltrosVendaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long clienteId;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    // Outros filtros podem ser adicionados aqui (ex: status da dívida associada)

    public FiltrosVendaDTO() {
    }

    public FiltrosVendaDTO(Long clienteId, LocalDate dataInicio, LocalDate dataFim) {
        this.clienteId = clienteId;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    // Getters e Setters
    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FiltrosVendaDTO that = (FiltrosVendaDTO) o;
        return Objects.equals(clienteId, that.clienteId) &&
                Objects.equals(dataInicio, that.dataInicio) &&
                Objects.equals(dataFim, that.dataFim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clienteId, dataInicio, dataFim);
    }

    @Override
    public String toString() {
        return "FiltrosVendaDTO{" +
                "clienteId=" + clienteId +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                '}';
    }
}