package br.com.api_caderneta.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class ItemVendaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idItemVenda;
    private String descricaoProduto;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;

    public ItemVendaDTO() {
    }

    public ItemVendaDTO(Long idItemVenda, String descricaoProduto, Integer quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {
        this.idItemVenda = idItemVenda;
        this.descricaoProduto = descricaoProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.subtotal = subtotal;
    }

    // Getters e Setters
    public Long getIdItemVenda() {
        return idItemVenda;
    }

    public void setIdItemVenda(Long idItemVenda) {
        this.idItemVenda = idItemVenda;
    }

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

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemVendaDTO that = (ItemVendaDTO) o;
        return Objects.equals(idItemVenda, that.idItemVenda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idItemVenda);
    }

    @Override
    public String toString() {
        return "ItemVendaDTO{" +
                "idItemVenda=" + idItemVenda +
                ", descricaoProduto='" + descricaoProduto + '\'' +
                ", quantidade=" + quantidade +
                ", subtotal=" + subtotal +
                '}';
    }
}