package br.com.api_caderneta.lojas;

import jakarta.persistence.*;
import java.time.Instant;

/** Loja (inquilino). Cada conta, cliente, venda e dívida pertence a uma loja. */
@Entity
@Table(name = "lojas")
public class Loja {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String nome;

  @Column(nullable = false)
  private boolean ativa = true;

  @Column(name = "criada_em", nullable = false)
  private Instant criadaEm = Instant.now();

  @Column(precision = 10, scale = 2, nullable = false)
  private java.math.BigDecimal limiteCreditoPadrao = java.math.BigDecimal.ZERO;
  @Column(length = 100)
  private String emailContato;
  private boolean emailConfirmado;
  public java.math.BigDecimal getLimiteCreditoPadrao() { return limiteCreditoPadrao; }
  public void setLimiteCreditoPadrao(java.math.BigDecimal valor) { limiteCreditoPadrao = valor; }
  public String getEmailContato() { return emailContato; }
  public void setEmailContato(String email) { emailContato = email; emailConfirmado = false; }
  public boolean isEmailConfirmado() { return emailConfirmado; }
  public void setEmailConfirmado(boolean confirmado) { emailConfirmado = confirmado; }
  protected Loja() {}

  public Loja(String nome) {
    this.nome = nome;
  }

  public Long getId() { return id; }
  public String getNome() { return nome; }
  public boolean isAtiva() { return ativa; }
}
