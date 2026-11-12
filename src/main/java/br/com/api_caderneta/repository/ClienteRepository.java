package br.com.api_caderneta.repository;

import br.com.api_caderneta.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository
    extends JpaRepository<Cliente, Long>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<Cliente> {
  @org.springframework.data.jpa.repository.Query(
      "select c from Cliente c where exists(select v.id from Venda v where v.cliente=c and"
          + " v.dataHora>=:inicioUtc and v.dataHora<:fimUtc) or exists(select d.id from Divida d"
          + " where d.cliente=c and d.dataEmissao between :inicio and :fim) order by c.nome,c.id")
  java.util.List<Cliente> doPeriodo(
      java.time.LocalDateTime inicioUtc,
      java.time.LocalDateTime fimUtc,
      java.time.LocalDate inicio,
      java.time.LocalDate fim);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select c from Cliente c where c.id = :id")
  java.util.Optional<Cliente> bloquear(
      @org.springframework.data.repository.query.Param("id") Long id);

  /**
   * Verifica se existe um cliente com o CPF fornecido. O Spring Data JPA implementará este método
   * automaticamente.
   *
   * @param cpf O CPF a ser verificado.
   * @return true se um cliente com o CPF existir, false caso contrário.
   */
  @org.springframework.data.jpa.repository.Query(
      // Só cadastros de CLIENTE: o mesmo CPF pode existir na equipe ou como fiador (CPF único por papel).
      "select count(c) > 0 from Cliente c where "
          + "replace(replace(replace(c.cpf, '.', ''), '-', ''), ' ', '') = "
          + "replace(replace(replace(:cpf, '.', ''), '-', ''), ' ', '')")
  boolean existsByCpf(@org.springframework.data.repository.query.Param("cpf") String cpf);
}
