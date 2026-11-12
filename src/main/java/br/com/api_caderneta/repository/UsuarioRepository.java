package br.com.api_caderneta.repository;

import br.com.api_caderneta.model.Usuario;
import br.com.api_caderneta.model.enums.Perfil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);
    boolean existsByLogin(String login);
    boolean existsByPessoaId(Long pessoaId);
    Optional<Usuario> findByPessoaId(Long pessoaId);
    /** Gerentes ativos DA LOJA (cada loja precisa manter pelo menos um). */
    long countByLojaIdAndPerfilAndAtivoTrue(Long lojaId, Perfil perfil);

    interface UsuarioResumo {
        Long getId();
        Long getPessoaId();
        String getNome();
        String getLogin();
        Perfil getPerfil();
        boolean isAtivo();
        java.time.Instant getExcluidoEm();
    }

    /** Contas da tela Acessos; as excluídas (exclusão lógica) só aparecem com excluidas=true. */
    @Query(value = "select u.id as id, u.pessoaId as pessoaId, p.nome as nome, u.login as login, "
            + "u.perfil as perfil, u.ativo as ativo, u.excluidoEm as excluidoEm "
            + "from Usuario u join Pessoa p on p.id = u.pessoaId "
            + "where u.lojaId = :loja and (:excluidas = true or u.excluidoEm is null) order by u.id",
            countQuery = "select count(u) from Usuario u where u.lojaId = :loja "
                    + "and (:excluidas = true or u.excluidoEm is null)")
    Page<UsuarioResumo> listar(@org.springframework.data.repository.query.Param("loja") Long loja,
                               @org.springframework.data.repository.query.Param("excluidas") boolean excluidas,
                               Pageable pageable);

    @Query("select p.nome from Pessoa p where p.id = :pessoaId")
    Optional<String> nomeDaPessoa(Long pessoaId);
}
