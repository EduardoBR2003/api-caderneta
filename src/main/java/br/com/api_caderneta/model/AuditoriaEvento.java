package br.com.api_caderneta.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "auditoria_eventos")
public class AuditoriaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    @Column(name = "usuario_id") private Long usuarioId;
    @Column(name = "ator_login", length = 50) private String atorLogin;
    @Column(length = 20) private String perfil;
    @Column(nullable = false, length = 60) private String acao;
    @Column(name = "recurso_tipo", nullable = false, length = 40) private String recursoTipo;
    @Column(name = "recurso_id") private Long recursoId;
    @Column(nullable = false, length = 20) private String resultado;
    @Column(columnDefinition = "TEXT") private String alteracoes;

    public AuditoriaEvento() {}

    public AuditoriaEvento(Instant ocorridoEm, Long usuarioId, String atorLogin, String perfil, String acao,
                           String recursoTipo, Long recursoId, String resultado, String alteracoes) {
        this.ocorridoEm = ocorridoEm; this.usuarioId = usuarioId; this.atorLogin = atorLogin; this.perfil = perfil;
        this.acao = acao; this.recursoTipo = recursoTipo; this.recursoId = recursoId;
        this.resultado = resultado; this.alteracoes = alteracoes;
    }

    public Long getId() { return id; }
    public Instant getOcorridoEm() { return ocorridoEm; }
    public Long getUsuarioId() { return usuarioId; }
    public String getAtorLogin() { return atorLogin; }
    public String getPerfil() { return perfil; }
    public String getAcao() { return acao; }
    public String getRecursoTipo() { return recursoTipo; }
    public Long getRecursoId() { return recursoId; }
    public String getResultado() { return resultado; }
    public String getAlteracoes() { return alteracoes; }
}
