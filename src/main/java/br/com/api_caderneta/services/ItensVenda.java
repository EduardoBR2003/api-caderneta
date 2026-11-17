package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.*;
import br.com.api_caderneta.exceptions.*;
import br.com.api_caderneta.model.*;
import br.com.api_caderneta.model.enums.*;
import br.com.api_caderneta.repository.*;
import br.com.api_caderneta.util.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.data.domain.*;

/** Regras do detalhamento; não consulta nem persiste venda ou dívida. */
public final class ItensVenda {
  private ItensVenda() {}

  public static BigDecimal validar(List<ItemVendaRequestDTO> itens, BigDecimal total) {
    BigDecimal soma = BigDecimal.ZERO;
    if (itens != null) {
      if (itens.size() > 200) throw new BusinessException("Informe no máximo 200 itens.");
      for (var i : itens) {
        if (i == null
            || i.getQuantidade() == null
            || i.getQuantidade() < 1
            || i.getQuantidade() > 1000000)
          throw new BusinessException("A quantidade deve estar entre 1 e 1000000.");
        if (i.getDescricaoProduto() == null
            || i.getDescricaoProduto().trim().isEmpty()
            || i.getDescricaoProduto().trim().length() > 200)
          throw new BusinessException("Informe a descrição do item (até 200 caracteres).");
        var preco = Regras.dinheiro(i.getPrecoUnitario(), false);
        if (preco.precision() > 10)
          throw new BusinessException("Preço unitário fora do limite permitido.");
        soma = soma.add(preco.multiply(BigDecimal.valueOf(i.getQuantidade())));
      }
    }
    if (soma.compareTo(total) > 0)
      throw new BusinessException("A soma dos itens não pode ultrapassar o total da venda.");
    return soma;
  }

  public static void aplicar(Venda v, List<ItemVendaRequestDTO> itens) {
    var novos = new ArrayList<ItemVenda>();
    if (itens != null)
      for (var i : itens) {
        var item = new ItemVenda();
        item.setDescricaoProduto(i.getDescricaoProduto().trim());
        item.setQuantidade(i.getQuantidade());
        item.setPrecoUnitario(i.getPrecoUnitario().setScale(2));
        novos.add(item);
      }
    v.setItensVenda(novos);
  }
}
