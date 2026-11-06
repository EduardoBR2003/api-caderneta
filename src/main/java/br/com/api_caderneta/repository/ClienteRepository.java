package br.com.api_caderneta.repository;

import br.com.api_caderneta.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Verifica se existe um cliente com o CPF fornecido.
     * O Spring Data JPA implementará este método automaticamente.
     *
     * @param cpf O CPF a ser verificado.
     * @return true se um cliente com o CPF existir, false caso contrário.
     */
    @org.springframework.data.jpa.repository.Query("select count(p) > 0 from Pessoa p where "
            + "replace(replace(replace(p.cpf, '.', ''), '-', ''), ' ', '') = "
            + "replace(replace(replace(:cpf, '.', ''), '-', ''), ' ', '')")
    boolean existsByCpf(@org.springframework.data.repository.query.Param("cpf") String cpf);
}