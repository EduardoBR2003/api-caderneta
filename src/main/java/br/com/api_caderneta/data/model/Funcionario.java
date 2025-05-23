package br.com.api_caderneta.data.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "funcionarios")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
// Se Funcionario também for um tipo de Usuario para login, os campos de Usuario estarão aqui.
// Se Proprietario é um Funcionario com mais papeis, a herança continua.
public class Funcionario extends Pessoa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(unique = true, length = 50)
    private String matricula;

    @Column(length = 50)
    private String cargo;

    // Campos para login (anteriormente na classe abstrata Usuario)
    @Column(nullable = false, unique = true, length = 50)
    private String login;

    @Column(nullable = false, length = 255) // Para armazenar hash da senha
    private String senhaHash;

    // Um funcionário pode registrar várias vendas
    @OneToMany(mappedBy = "funcionario", fetch = FetchType.LAZY)
    private List<Venda> vendasRegistradas = new ArrayList<>();


    public Funcionario(String nome, String cpf, String endereco, String email, String telefone, String matricula, String cargo, String login, String senhaHash) {
        super(nome, cpf, endereco, email, telefone);
        this.matricula = matricula;
        this.cargo = cargo;
        this.login = login;
        this.senhaHash = senhaHash;
    }
}