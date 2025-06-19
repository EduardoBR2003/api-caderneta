package br.com.api_caderneta.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "proprietarios") // A tabela 'proprietarios' se juntará à 'funcionarios'
public class Proprietario extends Funcionario implements Serializable {

    private static final long serialVersionUID = 1L;

    // Atributos específicos do proprietário, se houver.
    // Por exemplo:
    // private String nivelAcessoEspecial;

    public Proprietario() {
        super();
    }

    public Proprietario(String nome, String cpf, String endereco, String email, String telefone, String matricula, String cargo, String login, String senhaHash) {
        super(nome, cpf, endereco, email, telefone, matricula, cargo, login, senhaHash);
        // Inicializar atributos específicos do proprietário aqui, se houver
    }

    // Getters e Setters para atributos específicos (se houver)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        // Delega para a implementação de Funcionario, pois Proprietario pode não adicionar novos campos para equals.
        // Se Proprietario tivesse campos próprios que distinguem instâncias, eles seriam comparados aqui.
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        // Delega para a implementação de Funcionario.
        // Se Proprietario tivesse campos próprios, eles seriam incluídos no hash.
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Proprietario{" +
                "idPessoa=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", matricula='" + getMatricula() + '\'' +
                ", login='" + getLogin() + '\'' +
                '}';
    }
}