package br.com.api_caderneta.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "itens_venda")
public class ItemVenda implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idItemVenda;

    @Column(nullable = false, length = 200)
    private String descricaoProduto;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal; // quantidade * precoUnitario

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    public ItemVenda() {
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
        // Ao definir a quantidade ou preço unitário, o subtotal deve ser recalculado.
        // this.subtotal = calcularSubtotal(); // Pode ser feito aqui ou explicitamente chamado.
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
        // this.subtotal = calcularSubtotal(); // Pode ser feito aqui ou explicitamente chamado.
    }

    public BigDecimal getSubtotal() {
        // Garante que o subtotal seja calculado se não estiver definido.
        if (this.subtotal == null && this.quantidade != null && this.precoUnitario != null) {
            return calcularSubtotal();
        }
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    // Métodos de negócio [cite: 18]
    /**
     * Calcula e retorna o subtotal do item (quantidade * preço unitário).
     * @return O subtotal calculado.
     */
    public BigDecimal calcularSubtotal() {
        if (this.quantidade == null || this.precoUnitario == null) {
            return BigDecimal.ZERO;
        }
        return this.precoUnitario.multiply(new BigDecimal(this.quantidade));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemVenda itemVenda = (ItemVenda) o;
        return Objects.equals(idItemVenda, itemVenda.idItemVenda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idItemVenda);
    }

    @Override
    public String toString() {
        return "ItemVenda{" +
                "idItemVenda=" + idItemVenda +
                ", descricaoProduto='" + descricaoProduto + '\'' +
                ", quantidade=" + quantidade +
                ", precoUnitario=" + precoUnitario +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}