package br.com.api_caderneta.dto;

import java.io.Serializable;
import java.util.Objects;

public class FiadorDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private String endereco;

    public FiadorDTO() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FiadorDTO fiadorDTO = (FiadorDTO) o;
        return Objects.equals(id, fiadorDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}