package br.com.api_caderneta.catalogo;

import br.com.api_caderneta.lojas.*;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "itens_catalogo")
@EntityListeners(GuardaLoja.class)
public class ItemCatalogo implements ComLoja {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @org.hibernate.annotations.TenantId
  @Column(nullable = false, updatable = false) private Long lojaId;
  @Column(nullable = false, length = 200) private String descricao;
  @Column(precision = 10, scale = 2) private BigDecimal precoSugerido;
  private boolean ativo = true;
  public Long getId() { return id; }
  public Long getLojaId() { return lojaId; }
  public String getDescricao() { return descricao; }
  public void setDescricao(String v) { descricao = v; }
  public BigDecimal getPrecoSugerido() { return precoSugerido; }
  public void setPrecoSugerido(BigDecimal v) { precoSugerido = v; }
  public boolean isAtivo() { return ativo; }
  public void setAtivo(boolean v) { ativo = v; }
}
