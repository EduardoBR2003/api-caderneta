package br.com.api_caderneta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class FiadorRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull @NotEmpty
    private String nome;

    @NotNull @NotEmpty
    private String cpf;

    @Email
    private String email;

    private String telefone;
    private String endereco;

    public FiadorRequestDTO() {}

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
}