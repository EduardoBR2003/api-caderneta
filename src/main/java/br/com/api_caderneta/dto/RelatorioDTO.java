package br.com.api_caderneta.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public class RelatorioDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tipoRelatorio;
    private LocalDateTime dataGeracao;
    private Map<String, String> filtrosAplicados;
    private Object dados; // Dados do relatório (pode ser uma lista de DTOs específicos ou um objeto complexo)

    public RelatorioDTO() {
    }

    public RelatorioDTO(String tipoRelatorio, LocalDateTime dataGeracao, Map<String, String> filtrosAplicados, Object dados) {
        this.tipoRelatorio = tipoRelatorio;
        this.dataGeracao = dataGeracao;
        this.filtrosAplicados = filtrosAplicados;
        this.dados = dados;
    }

    // Getters e Setters
    public String getTipoRelatorio() {
        return tipoRelatorio;
    }

    public void setTipoRelatorio(String tipoRelatorio) {
        this.tipoRelatorio = tipoRelatorio;
    }

    public LocalDateTime getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(LocalDateTime dataGeracao) {
        this.dataGeracao = dataGeracao;
    }

    public Map<String, String> getFiltrosAplicados() {
        return filtrosAplicados;
    }

    public void setFiltrosAplicados(Map<String, String> filtrosAplicados) {
        this.filtrosAplicados = filtrosAplicados;
    }

    public Object getDados() {
        return dados;
    }

    public void setDados(Object dados) {
        this.dados = dados;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelatorioDTO that = (RelatorioDTO) o;
        return Objects.equals(tipoRelatorio, that.tipoRelatorio) &&
                Objects.equals(dataGeracao, that.dataGeracao) &&
                Objects.equals(filtrosAplicados, that.filtrosAplicados) &&
                Objects.equals(dados, that.dados);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipoRelatorio, dataGeracao, filtrosAplicados, dados);
    }

    @Override
    public String toString() {
        return "RelatorioDTO{" +
                "tipoRelatorio='" + tipoRelatorio + '\'' +
                ", dataGeracao=" + dataGeracao +
                '}';
    }
}