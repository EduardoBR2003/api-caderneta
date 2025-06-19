package br.com.api_caderneta.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

public class ItemVendaRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull @NotEmpty
    private String descricaoProduto;

    @NotNull @Min(1)
    private Integer quantidade;

    @NotNull
    private BigDecimal precoUnitario;

    public ItemVendaRequestDTO() {}

    // Getters e Setters
    public String getDescricaoProduto() { return descricaoProduto; }
    public void setDescricaoProduto(String descricaoProduto) { this.descricaoProduto = descricaoProduto; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }
}