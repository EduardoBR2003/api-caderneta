package br.com.api_caderneta.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class ItemVendaRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String descricaoProduto;
    private Integer quantidade;
    private BigDecimal precoUnitario;

    public ItemVendaRequestDTO() {
    }

    public ItemVendaRequestDTO(String descricaoProduto, Integer quantidade, BigDecimal precoUnitario) {
        this.descricaoProduto = descricaoProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    // Getters e Setters
    public String getDescricaoProduto() {
        return descricaoProduto;
    }

    public void setDescricaoProduto(String descricaoProduto) {
        this.descricaoProduto = descricaoProduto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemVendaRequestDTO that = (ItemVendaRequestDTO) o;
        return Objects.equals(descricaoProduto, that.descricaoProduto) &&
                Objects.equals(quantidade, that.quantidade) &&
                Objects.equals(precoUnitario, that.precoUnitario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(descricaoProduto, quantidade, precoUnitario);
    }

    @Override
    public String toString() {
        return "ItemVendaRequestDTO{" +
                "descricaoProduto='" + descricaoProduto + '\'' +
                ", quantidade=" + quantidade +
                ", precoUnitario=" + precoUnitario +
                '}';
    }
}