package br.com.api_caderneta.model;

import br.com.api_caderneta.model.enums.Perfil;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pessoa_id", nullable = false, unique = true)
    private Long pessoaId;

    /** Loja da conta. Sem @TenantId: o login é global e é procurado antes de se saber a loja. */
    @Column(name = "loja_id", nullable = false)
    private Long lojaId;

    @Column(nullable = false, unique = true, length = 50)
    private String login;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil;

    @Column(nullable = false)
    private boolean ativo = true;

    /** Exclusão lógica: a conta some da tela Acessos, mas o cadastro e o histórico continuam. */
    @Column(name = "excluido_em")
    private Instant excluidoEm;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    public Usuario() {}

    /** Conta na loja atual (usuário logado ou loja definida). */
    public Usuario(Long pessoaId, String login, String senhaHash, Perfil perfil) {
        this(br.com.api_caderneta.lojas.LojaAtual.raiz() ? null : br.com.api_caderneta.lojas.LojaAtual.id(),
                pessoaId, login, senhaHash, perfil);
    }

    public Usuario(Long lojaId, Long pessoaId, String login, String senhaHash, Perfil perfil) {
        this.lojaId = lojaId;
        this.pessoaId = pessoaId;
        this.login = login;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
    }

    public Long getId() { return id; }
    public Long getPessoaId() { return pessoaId; }
    public Long getLojaId() { return lojaId; }
    public String getLogin() { return login; }
    public String getSenhaHash() { return senhaHash; }
    public Perfil getPerfil() { return perfil; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public Instant getExcluidoEm() { return excluidoEm; }
    public void setExcluidoEm(Instant excluidoEm) { this.excluidoEm = excluidoEm; }
    public boolean isExcluido() { return excluidoEm != null; }
    public Instant getCriadoEm() { return criadoEm; }
}
