package br.com.api_caderneta.model;

import br.com.api_caderneta.model.enums.StatusDivida;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "dividas")
public class Divida implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDivida;

    @Column(nullable = false)
    private LocalDate dataEmissao;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorOriginal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorPendente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusDivida statusDivida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venda_origem_id", nullable = false, unique = true)
    private Venda vendaOrigem;

    @OneToMany(mappedBy = "divida", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Pagamento> pagamentos;

    public Divida() {
        this.pagamentos = new ArrayList<>();
        this.dataEmissao = LocalDate.now(); // Default para data atual
        this.statusDivida = StatusDivida.ABERTA; // Default status
    }

    // Getters e Setters
    public Long getIdDivida() {
        return idDivida;
    }

    public void setIdDivida(Long idDivida) {
        this.idDivida = idDivida;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
        atualizarStatus(); // Status pode mudar com base na nova data de vencimento
    }

    public BigDecimal getValorOriginal() {
        return valorOriginal;
    }

    public void setValorOriginal(BigDecimal valorOriginal) {
        this.valorOriginal = valorOriginal;
        // Ao definir o valor original, o valor pendente deve ser inicializado/atualizado
        // se ainda não houver pagamentos.
        if (this.pagamentos == null || this.pagamentos.isEmpty()) {
            this.valorPendente = valorOriginal;
        } else {
            this.valorPendente = calcularValorPendente();
        }
        atualizarStatus();
    }

    public BigDecimal getValorPendente() {
        return valorPendente;
    }

    /**
     * Define diretamente o valor pendente. [cite: 23]
     * Usar com cautela, pois geralmente é atualizado via adicionarPagamento.
     * @param valorPendente O novo valor pendente.
     */
    public void setValorPendente(BigDecimal valorPendente) {
        this.valorPendente = valorPendente;
        atualizarStatus(); // Atualiza o status com base no novo valor pendente
    }

    public StatusDivida getStatusDivida() {
        return statusDivida;
    }

    public void setStatusDivida(StatusDivida statusDivida) {
        this.statusDivida = statusDivida;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Venda getVendaOrigem() {
        return vendaOrigem;
    }

    public void setVendaOrigem(Venda vendaOrigem) {
        this.vendaOrigem = vendaOrigem;
    }

    public List<Pagamento> getPagamentos() {
        return pagamentos;
    }

    public void setPagamentos(List<Pagamento> pagamentos) {
        this.pagamentos = pagamentos;
        // Ao setar a lista de pagamentos, recalcular valor pendente e status
        this.valorPendente = calcularValorPendente();
        atualizarStatus();
    }

    // Métodos de negócio [cite: 19, 20, 21, 22]
    /**
     * Calcula e retorna o valor ainda pendente da dívida. [cite: 19]
     * @return O valor pendente.
     */
    public BigDecimal calcularValorPendente() {
        if (this.valorOriginal == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalPago = BigDecimal.ZERO;
        if (this.pagamentos != null) {
            for (Pagamento pagamento : this.pagamentos) {
                if (pagamento.getValorPago() != null) {
                    totalPago = totalPago.add(pagamento.getValorPago());
                }
            }
        }
        return this.valorOriginal.subtract(totalPago);
    }

    /**
     * Adiciona um pagamento à lista de pagamentos da dívida,
     * atualiza o valor pendente e o status da dívida. [cite: 21]
     * @param pagamento O pagamento a ser adicionado.
     */
    public void adicionarPagamento(Pagamento pagamento) {
        if (this.pagamentos == null) {
            this.pagamentos = new ArrayList<>();
        }
        this.pagamentos.add(pagamento);
        pagamento.setDivida(this); // Garante a bidirecionalidade

        this.valorPendente = calcularValorPendente(); // Atualiza o valor pendente
        atualizarStatus(); // Atualiza o status da dívida
    }

    /**
     * Atualiza o status da dívida com base no valor pendente e data de vencimento. [cite: 20]
     */
    public void atualizarStatus() {
        if (this.valorPendente == null) {
            this.valorPendente = calcularValorPendente();
        }

        if (this.valorPendente.compareTo(BigDecimal.ZERO) <= 0) {
            this.statusDivida = StatusDivida.PAGA_TOTALMENTE;
        } else if (this.valorPendente.compareTo(this.valorOriginal) < 0 && this.valorPendente.compareTo(BigDecimal.ZERO) > 0) {
            this.statusDivida = StatusDivida.PAGA_PARCIALMENTE;
        } else { // valorPendente == valorOriginal ou valorPendente > 0 (após outras verificações)
            this.statusDivida = StatusDivida.ABERTA;
        }

        // Verifica se está vencida após definir o status básico
        if (this.statusDivida != StatusDivida.PAGA_TOTALMENTE && estaVencidaSimpleCheck()) {
            this.statusDivida = StatusDivida.VENCIDA;
        }
    }

    // Método auxiliar para estaVencida e atualizarStatus, não considera valor pendente.
    private boolean estaVencidaSimpleCheck() {
        return this.dataVencimento != null && LocalDate.now().isAfter(this.dataVencimento);
    }

    /**
     * Verifica se a dívida está vencida (data de vencimento passou e ainda há saldo pendente). [cite: 22]
     * @return true se a dívida estiver vencida, false caso contrário.
     */
    public boolean estaVencida() {
        if (this.statusDivida == StatusDivida.PAGA_TOTALMENTE || this.statusDivida == StatusDivida.CANCELADA) {
            return false;
        }
        return estaVencidaSimpleCheck() && this.valorPendente != null && this.valorPendente.compareTo(BigDecimal.ZERO) > 0;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Divida divida = (Divida) o;
        return Objects.equals(idDivida, divida.idDivida);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDivida);
    }

    @Override
    public String toString() {
        return "Divida{" +
                "idDivida=" + idDivida +
                ", dataEmissao=" + dataEmissao +
                ", dataVencimento=" + dataVencimento +
                ", valorOriginal=" + valorOriginal +
                ", valorPendente=" + valorPendente +
                ", statusDivida=" + statusDivida +
                '}';
    }
}