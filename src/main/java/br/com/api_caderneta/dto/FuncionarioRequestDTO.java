package br.com.api_caderneta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

public class FuncionarioRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull @NotEmpty
    private String nome;

    @NotNull @NotEmpty
    private String cpf;

    @Email
    private String email;

    private String telefone;
    private String endereco;
    private String matricula;

    @NotNull @NotEmpty
    private String cargo;

    @NotNull @NotEmpty
    private String login;

    @NotNull @NotEmpty @Size(min = 6)
    private String senha;

    public FuncionarioRequestDTO() {}

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}