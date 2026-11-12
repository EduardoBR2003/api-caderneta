package br.com.api_caderneta.lojas;

import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import jakarta.persistence.PostLoad;
import java.util.Objects;

/**
 * Trava extra (além do filtro do @TenantId): nenhum registro de outra loja é carregado, nem por id
 * nem por associação. Responde "não encontrado" para não revelar que o registro existe.
 */
public class GuardaLoja {

  @PostLoad
  public void conferir(Object entidade) {
    if (!(entidade instanceof ComLoja e) || LojaAtual.raiz()) return;
    if (!Objects.equals(e.getLojaId(), LojaAtual.id())) {
      throw new ResourceNotFoundException("Registro não encontrado.");
    }
  }
}
