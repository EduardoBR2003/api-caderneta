package br.com.api_caderneta.dto;

import java.io.Serializable;
import java.util.Objects;

public class FuncionarioDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idPessoa;
    private String nome;
    private String cpf;
    private String endereco;
    private String email;
    private String telefone;
    private String matricula;
    private String cargo;
    private String login;

    public FuncionarioDTO() {
    }

    public FuncionarioDTO(Long idPessoa, String nome, String cpf, String endereco, String email, String telefone, String matricula, String cargo, String login) {
        this.idPessoa = idPessoa;
        this.nome = nome;
        this.cpf = cpf;
        this.endereco = endereco;
        this.email = email;
        this.telefone = telefone;
        this.matricula = matricula;
        this.cargo = cargo;
        this.login = login;
    }

    // Getters e Setters
    public Long getIdPessoa() {
        return idPessoa;
    }

    public void setIdPessoa(Long idPessoa) {
        this.idPessoa = idPessoa;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuncionarioDTO that = (FuncionarioDTO) o;
        return Objects.equals(idPessoa, that.idPessoa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPessoa);
    }

    @Override
    public String toString() {
        return "FuncionarioDTO{" +
                "idPessoa=" + idPessoa +
                ", nome='" + nome + '\'' +
                ", login='" + login + '\'' +
                ", matricula='" + matricula + '\'' +
                '}';
    }
}