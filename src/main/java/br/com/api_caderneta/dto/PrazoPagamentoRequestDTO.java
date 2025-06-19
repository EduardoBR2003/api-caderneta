package br.com.api_caderneta.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class PrazoPagamentoRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    @Min(0)
    private Integer novoPrazoPagamentoDias;

    public PrazoPagamentoRequestDTO() {}

    // Getters e Setters
    public Integer getNovoPrazoPagamentoDias() { return novoPrazoPagamentoDias; }
    public void setNovoPrazoPagamentoDias(Integer novoPrazoPagamentoDias) { this.novoPrazoPagamentoDias = novoPrazoPagamentoDias; }
}