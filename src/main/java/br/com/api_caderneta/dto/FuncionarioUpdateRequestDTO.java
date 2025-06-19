package br.com.api_caderneta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * DTO para receber dados na atualização de um funcionário.
 * Os campos são opcionais, permitindo a atualização parcial (PATCH).
 */
public class FuncionarioUpdateRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(min = 3, max = 150)
    private String nome;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String telefone;

    @Size(max = 200)
    private String endereco;

    @Size(max = 50)
    private String cargo;

    public FuncionarioUpdateRequestDTO() {
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
}