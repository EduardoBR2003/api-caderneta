package br.com.api_caderneta.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Cookie da sessão (Spring Session JDBC) declarado explicitamente. No Spring Boot 4.1 as propriedades
 * server.servlet.session.cookie.* não chegam ao cookie do Spring Session (verificado no teste ApiIT: o
 * cookie saía como SESSION, sem HttpOnly/Secure/SameSite). Lê as mesmas propriedades para manter um só
 * ponto de configuração (o perfil "dev" desliga o Secure).
 */
@Configuration(proxyBeanMethods = false)
public class SessaoConfig {

  @Bean
  CookieSerializer cookieSerializer(
      @Value("${server.servlet.session.cookie.name:SESSION}") String nome,
      @Value("${server.servlet.session.cookie.secure:true}") boolean seguro,
      @Value("${server.servlet.session.cookie.same-site:lax}") String sameSite,
      @Value("${server.servlet.session.cookie.path:/}") String caminho) {
    var s = new DefaultCookieSerializer();
    s.setCookieName(nome);
    s.setUseHttpOnlyCookie(true);
    s.setUseSecureCookie(seguro);
    s.setSameSite(sameSite.substring(0, 1).toUpperCase() + sameSite.substring(1).toLowerCase());
    s.setCookiePath(caminho);
    return s;
  }
}
