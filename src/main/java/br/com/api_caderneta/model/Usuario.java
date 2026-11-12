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

    @Column(nullable = false, unique = true, length = 50)
    private String login;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    public Usuario() {}

    public Usuario(Long pessoaId, String login, String senhaHash, Perfil perfil) {
        this.pessoaId = pessoaId;
        this.login = login;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
    }

    public Long getId() { return id; }
    public Long getPessoaId() { return pessoaId; }
    public String getLogin() { return login; }
    public String getSenhaHash() { return senhaHash; }
    public Perfil getPerfil() { return perfil; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public Instant getCriadoEm() { return criadoEm; }
}
