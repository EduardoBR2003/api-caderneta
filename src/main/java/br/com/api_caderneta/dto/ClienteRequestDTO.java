package br.com.api_caderneta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

public class ClienteRequestDTO implements Serializable {

  private static final long serialVersionUID = 1L;
  @jakarta.validation.Valid private FiadorRequestDTO fiador;
  @com.fasterxml.jackson.annotation.JsonIgnore
  private boolean fiadorPresente,
      emailPresente,
      telefonePresente,
      enderecoPresente,
      limitePresente,
      prazoPresente;

  public FiadorRequestDTO getFiador() {
    return fiador;
  }

  public void setFiador(FiadorRequestDTO f) {
    fiadorPresente = true;
    fiador = f;
  }

  public boolean temFiador() {
    return fiadorPresente;
  }

  public boolean temEmail() {
    return emailPresente;
  }

  public boolean temTelefone() {
    return telefonePresente;
  }

  public boolean temEndereco() {
    return enderecoPresente;
  }

  public boolean temLimite() {
    return limitePresente;
  }

  public boolean temPrazo() {
    return prazoPresente;
  }

  @NotNull
  @NotEmpty
  @Size(min = 3, max = 150)
  private String nome;

  @NotNull @NotEmpty private String cpf;

  @Email
  @Size(max = 100)
  private String email;

  @Size(max = 20)
  private String telefone;

  @Size(max = 200)
  private String endereco;

  private BigDecimal limiteCredito;
  private Integer prazoPagamentoPadraoDias;

  public ClienteRequestDTO() {}

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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
    emailPresente = true;
  }

  public String getTelefone() {
    return telefone;
  }

  public void setTelefone(String telefone) {
    this.telefone = telefone;
    telefonePresente = true;
  }

  public String getEndereco() {
    return endereco;
  }

  public void setEndereco(String endereco) {
    this.endereco = endereco;
    enderecoPresente = true;
  }

  public BigDecimal getLimiteCredito() {
    return limiteCredito;
  }

  public void setLimiteCredito(BigDecimal limiteCredito) {
    this.limiteCredito = limiteCredito;
    limitePresente = true;
  }

  public Integer getPrazoPagamentoPadraoDias() {
    return prazoPagamentoPadraoDias;
  }

  public void setPrazoPagamentoPadraoDias(Integer prazoPagamentoPadraoDias) {
    this.prazoPagamentoPadraoDias = prazoPagamentoPadraoDias;
    prazoPresente = true;
  }
}
