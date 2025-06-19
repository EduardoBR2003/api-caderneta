package br.com.api_caderneta.model;

import br.com.api_caderneta.model.enums.MetodoPagamento;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "pagamentos")
public class Pagamento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dataPagamento;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorPago;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MetodoPagamento metodoPagamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "divida_id", nullable = false)
    private Divida divida;

    public Pagamento() {
        this.dataPagamento = LocalDate.now(); // Default para data atual
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long idPagamento) {
        this.id = idPagamento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public void setValorPago(BigDecimal valorPago) {
        this.valorPago = valorPago;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public Divida getDivida() {
        return divida;
    }

    public void setDivida(Divida divida) {
        this.divida = divida;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pagamento pagamento = (Pagamento) o;
        return Objects.equals(id, pagamento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pagamento{" +
                "idPagamento=" + id +
                ", dataPagamento=" + dataPagamento +
                ", valorPago=" + valorPago +
                ", metodoPagamento=" + metodoPagamento +
                '}';
    }
}