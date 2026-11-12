package br.com.api_caderneta.security;

import br.com.api_caderneta.model.Usuario;
import br.com.api_caderneta.model.enums.Perfil;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/** Identidade guardada na sessão. O hash é apagado após a autenticação (CredentialsContainer). */
public class UsuarioPrincipal implements UserDetails, CredentialsContainer, Serializable {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final Long pessoaId;
    /** Loja da conta; null em sessões criadas antes das lojas existirem (exigem novo login). */
    private final Long lojaId;
    private final String login;
    private final Perfil perfil;
    private final boolean ativo;
    private String senhaHash;

    public UsuarioPrincipal(Usuario u) {
        this.id = u.getId();
        this.pessoaId = u.getPessoaId();
        this.lojaId = u.getLojaId();
        this.login = u.getLogin();
        this.perfil = u.getPerfil();
        this.ativo = u.isAtivo();
        this.senhaHash = u.getSenhaHash();
    }

    public Long getId() { return id; }
    public Long getPessoaId() { return pessoaId; }
    public Long getLojaId() { return lojaId; }
    public Perfil getPerfil() { return perfil; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }
    @Override public String getPassword() { return senhaHash; }
    @Override public String getUsername() { return login; }
    @Override public boolean isEnabled() { return ativo; }
    @Override public void eraseCredentials() { this.senhaHash = null; }
}
