package br.com.api_caderneta.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** Acesso à identidade da sessão atual. O responsável por uma operação vem daqui, nunca do corpo. */
public final class UsuarioAtual {
    private UsuarioAtual() {}

    public static Optional<UsuarioPrincipal> get() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.getPrincipal() instanceof UsuarioPrincipal p) return Optional.of(p);
        return Optional.empty();
    }

    public static UsuarioPrincipal obrigatorio() {
        return get().orElseThrow(() -> new IllegalStateException("Operação sem usuário autenticado."));
    }
}
