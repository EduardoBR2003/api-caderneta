package br.com.api_caderneta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

public class ClienteRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull @NotEmpty @Size(min = 3, max = 150)
    private String nome;

    @NotNull @NotEmpty
    private String cpf;

    @Email @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String telefone;

    @Size(max = 200)
    private String endereco;

    private BigDecimal limiteCredito;
    private Integer prazoPagamentoPadraoDias;

    public ClienteRequestDTO() {}

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
    public BigDecimal getLimiteCredito() { return limiteCredito; }
    public void setLimiteCredito(BigDecimal limiteCredito) { this.limiteCredito = limiteCredito; }
    public Integer getPrazoPagamentoPadraoDias() { return prazoPagamentoPadraoDias; }
    public void setPrazoPagamentoPadraoDias(Integer prazoPagamentoPadraoDias) { this.prazoPagamentoPadraoDias = prazoPagamentoPadraoDias; }
}