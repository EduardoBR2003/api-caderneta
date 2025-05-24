package br.com.api_caderneta.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class ClienteRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nome;
    private String cpf;
    private String endereco;
    private String email;
    private String telefone;
    private BigDecimal limiteCredito;
    private Integer prazoPagamentoPadraoDias;
    private Long fiadorId; // Opcional: ID do fiador a ser associado na criação

    public ClienteRequestDTO() {
    }

    public ClienteRequestDTO(String nome, String cpf, String endereco, String email, String telefone, BigDecimal limiteCredito, Integer prazoPagamentoPadraoDias, Long fiadorId) {
        this.nome = nome;
        this.cpf = cpf;
        this.endereco = endereco;
        this.email = email;
        this.telefone = telefone;
        this.limiteCredito = limiteCredito;
        this.prazoPagamentoPadraoDias = prazoPagamentoPadraoDias;
        this.fiadorId = fiadorId;
    }

    // Getters e Setters
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

    public BigDecimal getLimiteCredito() {
        return limiteCredito;
    }

    public void setLimiteCredito(BigDecimal limiteCredito) {
        this.limiteCredito = limiteCredito;
    }

    public Integer getPrazoPagamentoPadraoDias() {
        return prazoPagamentoPadraoDias;
    }

    public void setPrazoPagamentoPadraoDias(Integer prazoPagamentoPadraoDias) {
        this.prazoPagamentoPadraoDias = prazoPagamentoPadraoDias;
    }

    public Long getFiadorId() {
        return fiadorId;
    }

    public void setFiadorId(Long fiadorId) {
        this.fiadorId = fiadorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClienteRequestDTO that = (ClienteRequestDTO) o;
        return Objects.equals(nome, that.nome) &&
                Objects.equals(cpf, that.cpf) &&
                Objects.equals(endereco, that.endereco) &&
                Objects.equals(email, that.email) &&
                Objects.equals(telefone, that.telefone) &&
                Objects.equals(limiteCredito, that.limiteCredito) &&
                Objects.equals(prazoPagamentoPadraoDias, that.prazoPagamentoPadraoDias) &&
                Objects.equals(fiadorId, that.fiadorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, cpf, endereco, email, telefone, limiteCredito, prazoPagamentoPadraoDias, fiadorId);
    }

    @Override
    public String toString() {
        return "ClienteRequestDTO{" +
                "nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                '}';
    }
}