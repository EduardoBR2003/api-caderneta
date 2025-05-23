package br.com.api_caderneta.domain.model;

import br.com.api_caderneta.domain.enums.TipoVenda;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendas")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long idVenda;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoVenda tipoVenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER) // Eager para buscar itens com a venda
    private List<ItemVenda> itensVenda = new ArrayList<>();

    // Uma venda do tipo AFIADA pode gerar uma única dívida
    @OneToOne(mappedBy = "vendaOrigem", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private Divida dividaGerada;
}