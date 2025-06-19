package br.com.api_caderneta.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "clientes")
public class Cliente extends Pessoa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(precision = 10, scale = 2)
    private BigDecimal limiteCredito;

    private Integer prazoPagamentoPadraoDias; // Em dias

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiador_id")
    private Fiador fiador;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Venda> vendas;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Divida> dividas;

    public Cliente() {
        super();
        this.vendas = new ArrayList<>();
        this.dividas = new ArrayList<>();
    }

    public Cliente(String nome, String cpf, String endereco, String email, String telefone, BigDecimal limiteCredito, Integer prazoPagamentoPadraoDias) {
        super(nome, cpf, endereco, email, telefone);
        this.limiteCredito = limiteCredito;
        this.prazoPagamentoPadraoDias = prazoPagamentoPadraoDias;
        this.vendas = new ArrayList<>();
        this.dividas = new ArrayList<>();
    }

    // Getters e Setters
    public BigDecimal getLimiteCredito() {
        return limiteCredito;
    }

    /**
     * Atualiza o limite de crédito do cliente.
     * @param limiteCredito O novo limite de crédito.
     */
    public void setLimiteCredito(BigDecimal limiteCredito) {
        this.limiteCredito = limiteCredito;
    }

    public Integer getPrazoPagamentoPadraoDias() {
        return prazoPagamentoPadraoDias;
    }

    /**
     * Atualiza o prazo padrão de pagamento do cliente.
     * @param prazoPagamentoPadraoDias O novo prazo em dias.
     */
    public void setPrazoPagamentoPadraoDias(Integer prazoPagamentoPadraoDias) {
        this.prazoPagamentoPadraoDias = prazoPagamentoPadraoDias;
    }

    public Fiador getFiador() {
        return fiador;
    }

    /**
     * Associa ou desassocia um fiador ao cliente.
     * @param fiador O fiador a ser associado.
     */
    public void setFiador(Fiador fiador) {
        this.fiador = fiador;
    }

    public List<Venda> getVendas() {
        return vendas;
    }

    public void setVendas(List<Venda> vendas) {
        this.vendas = vendas;
    }

    public List<Divida> getDividas() {
        return dividas;
    }

    public void setDividas(List<Divida> dividas) {
        this.dividas = dividas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(getLimiteCredito(), cliente.getLimiteCredito()) &&
                Objects.equals(getPrazoPagamentoPadraoDias(), cliente.getPrazoPagamentoPadraoDias());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getLimiteCredito(), getPrazoPagamentoPadraoDias());
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "idPessoa=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", limiteCredito=" + limiteCredito +
                ", prazoPagamentoPadraoDias=" + prazoPagamentoPadraoDias +
                '}';
    }
}