package br.com.api_caderneta.email;

import br.com.api_caderneta.security.UsuarioPrincipal;
import jakarta.servlet.http.*;
import java.util.Set;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.config.annotation.*;

/** Verifica o estado atual em cada requisição, inclusive em sessões abertas antes da confirmação. */
@Configuration
public class LojaConfirmadaConfig implements WebMvcConfigurer {
  private final ConfirmacaoEmailService email;
  public LojaConfirmadaConfig(ConfirmacaoEmailService email) { this.email = email; }
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new HandlerInterceptor() {
      public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String p = req.getRequestURI();
        if (req.getMethod().equals("OPTIONS") || Set.of("/api/auth/login", "/api/auth/logout", "/api/auth/csrf", "/api/auth/me", "/api/auth/lojas", "/api/auth/registro-cliente", "/api/saude", "/api/saude/vivo").contains(p) || p.equals("/api/auth/email") || p.equals("/api/auth/email/solicitar") || p.equals("/api/auth/email/confirmar")) return true;
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal u && u.getLojaId() != null && !email.situacao(u.getLojaId()).confirmado()) {
          res.setStatus(403); res.setContentType("application/json;charset=UTF-8");
          res.getWriter().write("{\"code\":\"EMAIL_PENDENTE\",\"message\":\"Confirme o e-mail da loja antes de usar o sistema.\"}");
          return false;
        }
        return true;
      }
    }).addPathPatterns("/api/**", "/v3/api-docs/**", "/swagger-ui/**");
  }
}
