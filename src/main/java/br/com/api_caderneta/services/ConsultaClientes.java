package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.*;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.Cliente;
import br.com.api_caderneta.repository.ClienteRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ConsultaClientes {
  private final ClienteRepository repository;
  private final DataMapper mapper;
  private final org.springframework.jdbc.core.JdbcTemplate jdbc;

  public ConsultaClientes(
      ClienteRepository repository,
      DataMapper mapper,
      org.springframework.jdbc.core.JdbcTemplate jdbc) {
    this.repository = repository;
    this.mapper = mapper;
    this.jdbc = jdbc;
  }

  private record Saldo(java.math.BigDecimal valor, boolean atraso) {}

  private java.util.Map<Long, Saldo> saldos(List<Cliente> clientes) {
    if (clientes.isEmpty()) return java.util.Map.of();
    String marcadores = String.join(",", java.util.Collections.nCopies(clientes.size(), "?"));
    var args = new java.util.ArrayList<Object>();
    args.add(br.com.api_caderneta.util.Fuso.hoje());
    args.add(br.com.api_caderneta.lojas.LojaAtual.exigida());
    clientes.forEach(c -> args.add(c.getId()));
    return jdbc.query(
        "SELECT cliente_id,COALESCE(SUM(valor_pendente),0),MAX(CASE WHEN valor_pendente>0 AND"
            + " data_vencimento<? THEN 1 ELSE 0 END) FROM dividas WHERE status_divida<>'CANCELADA'"
            + " AND loja_id=? AND cliente_id IN ("
            + marcadores
            + ") GROUP BY cliente_id",
        (org.springframework.jdbc.core.ResultSetExtractor<java.util.Map<Long, Saldo>>)
            rs -> {
              var out = new java.util.HashMap<Long, Saldo>();
              while (rs.next())
                out.put(rs.getLong(1), new Saldo(rs.getBigDecimal(2), rs.getInt(3) == 1));
              return out;
            },
        args.toArray());
  }

  private ClienteResponseDTO resposta(Cliente c, java.util.Map<Long, Saldo> saldos) {
    var dto = mapper.parseObject(c, ClienteResponseDTO.class);
    var saldo = saldos.getOrDefault(c.getId(), new Saldo(java.math.BigDecimal.ZERO, false));
    dto.setSaldoDevedor(saldo.valor());
    dto.setEmAtraso(saldo.atraso());
    return dto;
  }

  @Transactional(readOnly = true)
  public br.com.api_caderneta.auth.Dtos.Pagina<ClienteResponseDTO> listar(
      int page, int size, String q) {
    org.springframework.data.jpa.domain.Specification<Cliente> spec =
        (root, query, cb) -> {
          String texto = q == null ? "" : q.strip().toLowerCase(java.util.Locale.ROOT);
          var nomes = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
          for (String parte : texto.split("\\s+"))
            nomes.add(
                cb.like(
                    cb.lower(root.get("nome")),
                    "%" + parte.replace("%", "\\%").replace("_", "\\_") + "%",
                    '\\'));
          String digitos = texto.replaceAll("\\D", "");
          if (digitos.isEmpty())
            return cb.and(nomes.toArray(jakarta.persistence.criteria.Predicate[]::new));
          jakarta.persistence.criteria.Expression<String> telefone = root.get("telefone");
          jakarta.persistence.criteria.Expression<String> cpf = root.get("cpf");
          for (String simbolo : java.util.List.of(".", "-", " ", "(", ")", "+")) {
            telefone =
                cb.function("replace", String.class, telefone, cb.literal(simbolo), cb.literal(""));
            cpf = cb.function("replace", String.class, cpf, cb.literal(simbolo), cb.literal(""));
          }
          return cb.or(
              cb.and(nomes.toArray(jakarta.persistence.criteria.Predicate[]::new)),
              cb.like(telefone, "%" + digitos + "%"),
              cb.like(cpf, "%" + digitos + "%"));
        };
    var pagina =
        repository.findAll(
            spec,
            org.springframework.data.domain.PageRequest.of(
                Math.max(0, page),
                Math.min(100, Math.max(1, size)),
                org.springframework.data.domain.Sort.by("nome", "id")));
    var totais = saldos(pagina.getContent());
    return br.com.api_caderneta.auth.Dtos.Pagina.de(pagina.map(c -> resposta(c, totais)));
  }
}
