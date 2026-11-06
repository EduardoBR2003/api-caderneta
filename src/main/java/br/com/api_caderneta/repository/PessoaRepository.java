package br.com.api_caderneta.repository;

import br.com.api_caderneta.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
    @org.springframework.data.jpa.repository.Query("select count(p) > 0 from Pessoa p where "
            + "replace(replace(replace(p.cpf, '.', ''), '-', ''), ' ', '') = "
            + "replace(replace(replace(:cpf, '.', ''), '-', ''), ' ', '')")
    boolean existsByCpf(@org.springframework.data.repository.query.Param("cpf") String cpf);
}