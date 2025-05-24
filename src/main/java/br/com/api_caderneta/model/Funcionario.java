package br.com.api_caderneta.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "funcionarios")
public class Funcionario extends Pessoa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(unique = true, length = 50)
    private String matricula;

    @Column(length = 50)
    private String cargo;

    @Column(nullable = false, unique = true, length = 50)
    private String login;

    @Column(nullable = false, length = 255) // Para armazenar hash da senha
    private String senhaHash;

    @OneToMany(mappedBy = "funcionario", fetch = FetchType.LAZY)
    private List<Venda> vendasRegistradas;

    public Funcionario() {
        super();
        this.vendasRegistradas = new ArrayList<>();
    }

    public Funcionario(String nome, String cpf, String endereco, String email, String telefone, String matricula, String cargo, String login, String senhaHash) {
        super(nome, cpf, endereco, email, telefone);
        this.matricula = matricula;
        this.cargo = cargo;
        this.login = login;
        this.senhaHash = senhaHash;
        this.vendasRegistradas = new ArrayList<>();
    }

    // Getters e Setters
    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public List<Venda> getVendasRegistradas() {
        return vendasRegistradas;
    }

    public void setVendasRegistradas(List<Venda> vendasRegistradas) {
        this.vendasRegistradas = vendasRegistradas;
    }

    // Métodos de negócio [cite: 14]
    /**
     * Compara a senha fornecida (após hashing) com o hash da senha armazenada para o usuário.
     * NOTA: A lógica de hashing real deve ser implementada aqui ou delegada a um serviço.
     * Esta é uma implementação placeholder.
     * @param senhaFornecida A senha em texto plano a ser verificada.
     * @return true se a senha coincidir, false caso contrário.
     */
    public boolean verificarSenha(String senhaFornecida) {
        // Em um cenário real, 'senhaFornecida' seria hasheada aqui usando o mesmo
        // algoritmo e salt que foi usado para gerar 'this.senhaHash'.
        // Exemplo simplificado (NÃO SEGURO PARA PRODUÇÃO):
        // PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // return passwordEncoder.matches(senhaFornecida, this.senhaHash);
        if (senhaFornecida == null || this.senhaHash == null) {
            return false;
        }
        // Esta é uma comparação placeholder e NÃO é segura.
        // Deve ser substituída por uma comparação de hash segura.
        return this.senhaHash.equals(senhaFornecida); // Placeholder
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Funcionario that = (Funcionario) o;
        return Objects.equals(getMatricula(), that.getMatricula()) &&
                Objects.equals(getLogin(), that.getLogin());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getMatricula(), getLogin());
    }

    @Override
    public String toString() {
        return "Funcionario{" +
                "idPessoa=" + getIdPessoa() +
                ", nome='" + getNome() + '\'' +
                ", matricula='" + matricula + '\'' +
                ", login='" + login + '\'' +
                '}';
    }
}