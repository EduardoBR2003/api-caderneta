package br.com.api_caderneta.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

public class LimiteCreditoRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private BigDecimal novoLimiteCredito;

    public LimiteCreditoRequestDTO() {}

    // Getters e Setters
    public BigDecimal getNovoLimiteCredito() { return novoLimiteCredito; }
    public void setNovoLimiteCredito(BigDecimal novoLimiteCredito) { this.novoLimiteCredito = novoLimiteCredito; }
}