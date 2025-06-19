package br.com.api_caderneta.dto;

import br.com.api_caderneta.model.enums.StatusDivida;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

public class RelatorioRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private LocalDate dataInicio;

    @NotNull
    private LocalDate dataFim;

    private StatusDivida status;

    public RelatorioRequestDTO() {}

    // Getters e Setters
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public StatusDivida getStatus() { return status; }
    public void setStatus(StatusDivida status) { this.status = status; }
}