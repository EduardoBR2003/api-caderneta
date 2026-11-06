package br.com.api_caderneta.util;

import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.model.enums.StatusDivida;
import jakarta.persistence.criteria.*;

public final class Consultas {
  private Consultas() {}

  public static Predicate status(CriteriaBuilder cb, From<?, ?> d, String valor) {
    StatusDivida s;
    try {
      s = StatusDivida.valueOf(valor);
    } catch (IllegalArgumentException e) {
      throw new BusinessException("Status inválido.");
    }
    var cancelada = cb.equal(d.get("statusDivida"), StatusDivida.CANCELADA);
    if (s == StatusDivida.CANCELADA) return cancelada;
    var ativa = cb.not(cancelada);
    var saldo =
        cb.greaterThan(d.<java.math.BigDecimal>get("valorPendente"), java.math.BigDecimal.ZERO);
    var vencida = cb.lessThan(d.<java.time.LocalDate>get("dataVencimento"), Fuso.hoje());
    if (s == StatusDivida.VENCIDA) return cb.and(ativa, saldo, vencida);
    if (s == StatusDivida.PAGA_TOTALMENTE) return cb.and(ativa, cb.not(saldo));
    var parcial = cb.lessThan(d.<java.math.BigDecimal>get("valorPendente"), d.get("valorOriginal"));
    return cb.and(
        ativa,
        saldo,
        cb.not(vencida),
        s == StatusDivida.PAGA_PARCIALMENTE ? parcial : cb.not(parcial));
  }
}
