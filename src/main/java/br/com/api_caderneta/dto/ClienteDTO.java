package br.com.api_caderneta.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class ClienteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private String endereco;
    private BigDecimal limiteCredito;
    private Integer prazoPagamentoPadraoDias;

    public ClienteDTO() {}

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
    public BigDecimal getLimiteCredito() { return limiteCredito; }
    public void setLimiteCredito(BigDecimal limiteCredito) { this.limiteCredito = limiteCredito; }
    public Integer getPrazoPagamentoPadraoDias() { return prazoPagamentoPadraoDias; }
    public void setPrazoPagamentoPadraoDias(Integer prazoPagamentoPadraoDias) { this.prazoPagamentoPadraoDias = prazoPagamentoPadraoDias; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClienteDTO that = (ClienteDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}