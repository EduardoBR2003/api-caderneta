package br.com.api_caderneta.security;

import jakarta.servlet.DispatcherType;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
    return c.getAuthenticationManager();
  }

  /** Token CSRF guardado na sessão do servidor; cabeçalho padrão X-CSRF-TOKEN. */
  @Bean
  CsrfTokenRepository csrfTokenRepository() {
    return new HttpSessionCsrfTokenRepository();
  }

  @Bean
  SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  /**
   * Login manual: renova o id da sessão e o token CSRF, como no login padrão do Spring Security.
   */
  @Bean
  SessionAuthenticationStrategy sessionAuthenticationStrategy(CsrfTokenRepository csrfRepo) {
    return new CompositeSessionAuthenticationStrategy(
        List.of(
            new ChangeSessionIdAuthenticationStrategy(), new CsrfAuthenticationStrategy(csrfRepo)));
  }

  /**
   * CORS só para desenvolvimento com outra origem (flutter run). Vazio = sem CORS (mesma origem).
   */
  @Bean
  CorsConfigurationSource corsConfigurationSource(
      @Value("${caderneta.cors.allowed-origins:}") List<String> origens) {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    List<String> permitidas = origens.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
    if (!permitidas.isEmpty()) {
      CorsConfiguration cfg = new CorsConfiguration();
      cfg.setAllowedOrigins(permitidas);
      cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
      cfg.setAllowedHeaders(List.of("Content-Type", "Accept", "X-CSRF-TOKEN", "Idempotency-Key"));
      cfg.setAllowCredentials(true);
      cfg.setMaxAge(600L);
      source.registerCorsConfiguration("/api/**", cfg);
    }
    return source;
  }

  @Bean
  SecurityFilterChain filterChain(
      HttpSecurity http,
      CsrfTokenRepository csrfRepo,
      SecurityContextRepository contextRepo,
      ApiSecurityHandlers handlers)
      throws Exception {
    http.cors(c -> {})
        .csrf(c -> c.csrfTokenRepository(csrfRepo))
        .securityContext(c -> c.securityContextRepository(contextRepo))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .requestCache(c -> c.disable())
        .exceptionHandling(e -> e.authenticationEntryPoint(handlers).accessDeniedHandler(handlers))
        .authorizeHttpRequests(
            a ->
                a.dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD)
                    .permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/api/**")
                    .permitAll()
                    // Autenticação
                    .requestMatchers(HttpMethod.GET, "/api/auth/csrf", "/api/saude", "/api/saude/vivo")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.POST, "/api/auth/login", "/api/auth/registro-cliente")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/auth/me")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/auth/logout")
                    .authenticated()
                    // Somente gerente
                    .requestMatchers(HttpMethod.POST, "/api/auth/funcionarios")
                    .hasRole("GERENTE")
                    .requestMatchers("/api/usuarios/**", "/api/relatorios/**", "/api/auditoria/**")
                    .hasRole("GERENTE")
                    .requestMatchers(HttpMethod.PUT, "/api/dividas/{id}")
                    .hasRole("GERENTE")
                    .requestMatchers(
                        HttpMethod.PATCH,
                        "/api/clientes/{id}/limite-credito",
                        "/api/clientes/{id}/prazo-pagamento")
                    .hasRole("GERENTE")
                    .requestMatchers(
                        HttpMethod.DELETE,
                        "/api/vendas/{id}",
                        "/api/dividas/{id}",
                        "/api/clientes/{id}")
                    .hasRole("GERENTE")
                    // Rotas legadas de funcionário/proprietário: a criação grava senha fora de
                    // usuarios e foi
                    // substituída por POST /api/auth/funcionarios. Demais operações: gerente.
                    .requestMatchers(HttpMethod.POST, "/api/funcionarios", "/api/v1/proprietarios")
                    .denyAll()
                    .requestMatchers("/api/funcionarios/**", "/api/v1/proprietarios/**")
                    .hasRole("GERENTE")
                    // Legado: expõe eventos de todos os clientes e tipos; só o gerente.
                    .requestMatchers("/api/notificacoes/**")
                    .hasRole("GERENTE")
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                    .hasRole("GERENTE")
                    // Área do cliente e histórico
                    .requestMatchers("/api/me/**")
                    .hasRole("CLIENTE")
                    .requestMatchers("/api/historico/**")
                    .hasAnyRole("GERENTE", "FUNCIONARIO")
                    .requestMatchers("/api/resumo")
                    .hasAnyRole("GERENTE", "FUNCIONARIO")
                    // Operação (gerente e funcionário), incluindo POST de venda, pagamento e
                    // convite
                    .requestMatchers(
                        "/api/clientes/**", "/api/fiadores/**", "/api/vendas/**", "/api/dividas/**")
                    .hasAnyRole("GERENTE", "FUNCIONARIO")
                    .anyRequest()
                    .denyAll());
    return http.build();
  }
}
