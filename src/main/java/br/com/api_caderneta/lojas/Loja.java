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

  protected Loja() {}

  public Loja(String nome) {
    this.nome = nome;
  }

  public Long getId() { return id; }
  public String getNome() { return nome; }
  public boolean isAtiva() { return ativa; }
}
