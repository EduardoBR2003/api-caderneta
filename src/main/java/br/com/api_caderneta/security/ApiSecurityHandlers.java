package br.com.api_caderneta.security;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper mapper;

    public ApiSecurityHandlers(ObjectMapper mapper) { this.mapper = mapper; }

    /** Sem sessão válida: JSON 401, nunca redirecionamento para uma página de login. */
    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
            throws IOException {
        ApiErrors.write(mapper, res, 401, "NAO_AUTENTICADO", "Sua sessão expirou. Entre novamente.");
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex)
            throws IOException {
        if (ex instanceof CsrfException) {
            // Sessão expirada em rota protegida: o CsrfFilter roda antes da autorização e veria só
            // "CSRF inválido". O cliente precisa saber que foi a sessão que acabou.
            if (!autenticado() && !rotaPublica(req)) {
                ApiErrors.write(mapper, res, 401, "NAO_AUTENTICADO", "Sua sessão expirou. Entre novamente.");
                return;
            }
            ApiErrors.write(mapper, res, 403, "CSRF_INVALID",
                    "A validação de segurança expirou. Tente novamente.");
            return;
        }
        ApiErrors.write(mapper, res, 403, "ACESSO_NEGADO", "Acesso negado.");
    }

    private static boolean autenticado() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.isAuthenticated() && !(a instanceof AnonymousAuthenticationToken);
    }

    private static boolean rotaPublica(HttpServletRequest req) {
        String p = req.getRequestURI();
        return p.equals("/api/auth/login") || p.equals("/api/auth/registro-cliente");
    }
}
