package br.com.api_caderneta.repository;

import br.com.api_caderneta.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    Optional<Funcionario> findByLogin(String login);

    // Só cadastros da EQUIPE: o mesmo CPF pode existir como cliente ou fiador (CPF único por papel).
    @org.springframework.data.jpa.repository.Query("select count(f) > 0 from Funcionario f where "
            + "replace(replace(replace(f.cpf, '.', ''), '-', ''), ' ', '') = "
            + "replace(replace(replace(:cpf, '.', ''), '-', ''), ' ', '')")
    Boolean existsByCpf(@org.springframework.data.repository.query.Param("cpf") String cpf);

    @org.springframework.data.jpa.repository.Query("select f from Funcionario f where "
            + "replace(replace(replace(f.cpf, '.', ''), '-', ''), ' ', '') = "
            + "replace(replace(replace(:cpf, '.', ''), '-', ''), ' ', '')")
    Optional<Funcionario> porCpf(@org.springframework.data.repository.query.Param("cpf") String cpf);

    Boolean existsByLogin(String login);

    Boolean existsByMatricula(String matricula);
}