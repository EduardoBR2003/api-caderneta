package br.com.api_caderneta.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class FiltroRelatorioDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String tipoEspecifico; // Ex: status do cliente para "RelatorioClientesAtivos"
    // Outros filtros conforme a necessidade de cada relatório

    public FiltroRelatorioDTO() {
    }

    public FiltroRelatorioDTO(LocalDate dataInicio, LocalDate dataFim, String tipoEspecifico) {
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.tipoEspecifico = tipoEspecifico;
    }

    // Getters e Setters
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

    public String getTipoEspecifico() {
        return tipoEspecifico;
    }

    public void setTipoEspecifico(String tipoEspecifico) {
        this.tipoEspecifico = tipoEspecifico;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FiltroRelatorioDTO that = (FiltroRelatorioDTO) o;
        return Objects.equals(dataInicio, that.dataInicio) &&
                Objects.equals(dataFim, that.dataFim) &&
                Objects.equals(tipoEspecifico, that.tipoEspecifico);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataInicio, dataFim, tipoEspecifico);
    }

    @Override
    public String toString() {
        return "FiltroRelatorioDTO{" +
                "dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                ", tipoEspecifico='" + tipoEspecifico + '\'' +
                '}';
    }
}