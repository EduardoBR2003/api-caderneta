package br.com.api_caderneta.data.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Entity
@Table(name = "proprietarios") // A tabela 'proprietarios' se juntará à 'funcionarios'
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Proprietario extends Funcionario implements Serializable {

    private static final long serialVersionUID = 1L;

    // Atributos específicos do proprietário, se houver.
    // Por exemplo:
    // private String nivelAcessoEspecial;

    public Proprietario(String nome, String cpf, String endereco, String email, String telefone, String matricula, String cargo, String login, String senhaHash) {
        super(nome, cpf, endereco, email, telefone, matricula, cargo, login, senhaHash);
        // Inicializar atributos específicos do proprietário aqui, se houver
    }
}