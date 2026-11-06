package br.com.api_caderneta.controller;

import br.com.api_caderneta.model.enums.Perfil;
import br.com.api_caderneta.security.UsuarioAtual;
import br.com.api_caderneta.util.Fuso;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
public class ResumoController {
  private final JdbcTemplate jdbc;

  public ResumoController(JdbcTemplate j) {
    jdbc = j;
  }

  @GetMapping("/api/relatorios/resumo")
  public Map<String, Object> relatorio(
      @RequestParam java.time.LocalDate inicio, @RequestParam java.time.LocalDate fim) {
    if (inicio == null || fim == null || inicio.isAfter(fim))
      throw new br.com.api_caderneta.exceptions.BusinessException("Informe um período válido.");
    var r =
        new LinkedHashMap<String, Object>(
            jdbc.queryForMap(
                "select coalesce(sum(valor_total),0) vendido,count(*)"
                    + " quantidadeVendas,count(distinct cliente_id) clientesAtivos from vendas"
                    + " where data_hora>=? and data_hora<?",
                Fuso.inicioDoDiaUtc(inicio),
                Fuso.inicioDoDiaUtc(fim.plusDays(1))));
    r.put(
        "recebido",
        jdbc.queryForObject(
            "select coalesce(sum(valor_pago),0) from pagamentos where data_pagamento between ? and"
                + " ?",
            BigDecimal.class,
            inicio,
            fim));
    r.putAll(
        jdbc.queryForMap(
            "select coalesce(sum(valor_pendente),0) saldo,coalesce(sum(case when data_vencimento<?"
                + " then valor_pendente else 0 end),0) vencido from dividas where"
                + " status_divida<>'CANCELADA' and data_emissao between ? and ?",
            Fuso.hoje(),
            inicio,
            fim));
    BigDecimal saldo = (BigDecimal) r.get("saldo"), vencido = (BigDecimal) r.get("vencido");
    r.put(
        "inadimplencia",
        saldo.signum() == 0
            ? BigDecimal.ZERO
            : vencido
                .multiply(BigDecimal.valueOf(100))
                .divide(saldo, 4, java.math.RoundingMode.HALF_UP));
    return r;
  }

  @GetMapping({"/api/resumo", "/api/me/resumo"})
  public Map<String, Object> resumo() {
    var u = UsuarioAtual.obrigatorio();
    boolean cliente = u.getPerfil() == Perfil.CLIENTE;
    String filtro = cliente ? " and cliente_id=?" : "";
    Object[] args =
        cliente ? new Object[] {Fuso.hoje(), u.getPessoaId()} : new Object[] {Fuso.hoje()};
    var r =
        new LinkedHashMap<String, Object>(
            jdbc.queryForMap(
                "select coalesce(sum(valor_pendente),0) saldo, coalesce(sum(case when"
                    + " data_vencimento<? and valor_pendente>0 then 1 else 0 end),0) vencidas,"
                    + " min(case when valor_pendente>0 then data_vencimento end) proximoVencimento"
                    + " from dividas where status_divida<>'CANCELADA'"
                    + filtro,
                args));
    var inicio = Fuso.hoje().withDayOfMonth(1);
    var fim = inicio.plusMonths(1);
    String vendasFiltro =
        cliente
            ? " and cliente_id=?"
            : u.getPerfil() == Perfil.FUNCIONARIO ? " and funcionario_id=?" : "";
    var va = new ArrayList<Object>(List.of(Fuso.inicioDoDiaUtc(inicio), Fuso.inicioDoDiaUtc(fim)));
    if (!vendasFiltro.isEmpty()) va.add(u.getPessoaId());
    r.put(
        "vendasMes",
        jdbc.queryForObject(
            "select coalesce(sum(valor_total),0) from vendas where data_hora>=? and data_hora<?"
                + vendasFiltro,
            BigDecimal.class,
            va.toArray()));
    var pa = new ArrayList<Object>(List.of(inicio, fim));
    if (cliente) pa.add(u.getPessoaId());
    r.put(
        "recebidoMes",
        jdbc.queryForObject(
            "select coalesce(sum(p.valor_pago),0) from pagamentos p join dividas d on"
                + " d.id=p.divida_id where p.data_pagamento>=? and p.data_pagamento<?"
                + (cliente ? " and d.cliente_id=?" : ""),
            BigDecimal.class,
            pa.toArray()));
    return r;
  }
}
