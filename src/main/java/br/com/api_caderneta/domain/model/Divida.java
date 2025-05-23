package br.com.api_caderneta.domain.model;

import br.com.api_caderneta.domain.enums.StatusDivida;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dividas")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Divida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

    // Uma dívida é originada por uma única venda (do tipo AFIADA)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venda_origem_id", nullable = false, unique = true)
    private Venda vendaOrigem;

    @OneToMany(mappedBy = "divida", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Pagamento> pagamentos = new ArrayList<>();
}