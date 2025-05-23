package br.com.api_caderneta.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Cliente extends Pessoa {

    @Column(precision = 10, scale = 2)
    private BigDecimal limiteCredito;

    private Integer prazoPagamentoPadraoDias; // Em dias

    // Um cliente é afiançado por um fiador (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiador_id")
    private Fiador fiador;

    // Um cliente pode ter várias vendas
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Venda> vendas = new ArrayList<>();

    // Um cliente pode ter várias dívidas
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Divida> dividas = new ArrayList<>();


    public Cliente(String nome, String cpf, String endereco, String email, String telefone, BigDecimal limiteCredito, Integer prazoPagamentoPadraoDias) {
        super(nome, cpf, endereco, email, telefone);
        this.limiteCredito = limiteCredito;
        this.prazoPagamentoPadraoDias = prazoPagamentoPadraoDias;
    }
}