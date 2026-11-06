package br.com.api_caderneta.repository;

import br.com.api_caderneta.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    Optional<Funcionario> findByLogin(String login);

    @org.springframework.data.jpa.repository.Query("select count(p) > 0 from Pessoa p where "
            + "replace(replace(replace(p.cpf, '.', ''), '-', ''), ' ', '') = "
            + "replace(replace(replace(:cpf, '.', ''), '-', ''), ' ', '')")
    Boolean existsByCpf(@org.springframework.data.repository.query.Param("cpf") String cpf);

    Boolean existsByLogin(String login);

    Boolean existsByMatricula(String matricula);
}