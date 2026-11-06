package br.com.api_caderneta;

public final class TestAuth {
  public static void entrar(long pessoa, br.com.api_caderneta.model.enums.Perfil perfil) {
    var u = new br.com.api_caderneta.model.Usuario(pessoa, "teste", "x", perfil);
    org.springframework.test.util.ReflectionTestUtils.setField(u, "id", pessoa);
    var p = new br.com.api_caderneta.security.UsuarioPrincipal(u);
    org.springframework.security.core.context.SecurityContextHolder.getContext()
        .setAuthentication(
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                .authenticated(p, null, p.getAuthorities()));
  }
}
