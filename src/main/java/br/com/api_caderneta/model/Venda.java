package br.com.api_caderneta.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "vendas")
public class Venda implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVenda;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemVenda> itensVenda;

    @OneToOne(mappedBy = "vendaOrigem", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private Divida dividaGerada;

    public Venda() {
        this.itensVenda = new ArrayList<>();
        this.dataHora = LocalDateTime.now(); // Default data/hora para o momento da criação
        this.valorTotal = BigDecimal.ZERO; // Default valor total
    }

    // Getters e Setters
    public Long getIdVenda() {
        return idVenda;
    }

    public void setIdVenda(Long idVenda) {
        this.idVenda = idVenda;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public BigDecimal getValorTotal() {
        // Poderia recalcular aqui se necessário, mas o cálculo está em calcularValorTotal()
        // e adicionarItemVenda()
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }


    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public List<ItemVenda> getItensVenda() {
        return itensVenda;
    }

    public void setItensVenda(List<ItemVenda> itensVenda) {
        this.itensVenda = itensVenda;
        // Ao setar a lista de itens, recalcular o valor total
        this.valorTotal = calcularValorTotal();
    }

    public Divida getDividaGerada() {
        return dividaGerada;
    }

    /**
     * Associa a dívida gerada a esta venda. [cite: 17]
     * @param dividaGerada A dívida gerada.
     */
    public void setDividaGerada(Divida dividaGerada) {
        this.dividaGerada = dividaGerada;
        if (dividaGerada != null) {
            dividaGerada.setVendaOrigem(this); // Mantém a bidirecionalidade
        }
    }

    // Métodos de negócio [cite: 15, 16]
    /**
     * Calcula e retorna o valor total da venda com base na soma dos subtotais de seus ItemVenda. [cite: 15]
     * @return O valor total da venda.
     */
    public BigDecimal calcularValorTotal() {
        if (this.itensVenda == null || this.itensVenda.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (ItemVenda item : this.itensVenda) {
            item.setSubtotal(item.calcularSubtotal()); // Garante que o subtotal do item esteja atualizado
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    /**
     * Adiciona um item à venda, associa o item a esta venda e recalcula o valor total. [cite: 16]
     * @param item O item a ser adicionado.
     */
    public void adicionarItemVenda(ItemVenda item) {
        if (this.itensVenda == null) {
            this.itensVenda = new ArrayList<>();
        }
        this.itensVenda.add(item);
        item.setVenda(this); // Associa o item a esta venda
        item.setSubtotal(item.calcularSubtotal()); // Calcula o subtotal do item adicionado
        this.valorTotal = calcularValorTotal(); // Recalcula o valor total da venda
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Venda venda = (Venda) o;
        return Objects.equals(idVenda, venda.idVenda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idVenda);
    }

    @Override
    public String toString() {
        return "Venda{" +
                "idVenda=" + idVenda +
                ", dataHora=" + dataHora +
                ", valorTotal=" + valorTotal +
                '}';
    }
}