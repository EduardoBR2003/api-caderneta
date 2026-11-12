package br.com.api_caderneta.lojas;

import java.util.Map;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

/**
 * Liga o @TenantId do Hibernate à loja atual: toda consulta JPA às entidades com loja ganha
 * "loja_id = loja atual" e todo INSERT recebe a loja atual. A RAIZ (sem usuário) não filtra.
 */
@Component
public class LojaTenantResolver
    implements CurrentTenantIdentifierResolver<Long>, HibernatePropertiesCustomizer {

  @Override
  public Long resolveCurrentTenantIdentifier() {
    return LojaAtual.id();
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return false;
  }

  @Override
  public boolean isRoot(Long loja) {
    return LojaAtual.RAIZ.equals(loja);
  }

  @Override
  public void customize(Map<String, Object> props) {
    props.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
  }
}
