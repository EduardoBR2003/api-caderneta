package br.com.api_caderneta.lojas;

import br.com.api_caderneta.security.UsuarioPrincipal;
import java.util.function.Supplier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Loja (inquilino) da operação atual. Ordem: loja definida explicitamente (cadastro de loja, gerente
 * inicial) > loja do usuário logado > RAIZ (sem usuário: login, convite, tarefas do sistema), que
 * enxerga todas as lojas.
 */
public final class LojaAtual {

  /** Nenhuma loja tem id 0: é a "raiz" do Hibernate (sem filtro). */
  public static final Long RAIZ = 0L;

  private static final ThreadLocal<Long> DEFINIDA = new ThreadLocal<>();

  private LojaAtual() {}

  /**
   * Evita recursão: ler o usuário logado pode carregar a sessão do banco (Spring Session), o que abre
   * outra transação que pergunta de novo pela loja. Essa leitura interna (só a tabela de sessões)
   * roda como RAIZ.
   */
  private static final ThreadLocal<Boolean> RESOLVENDO = ThreadLocal.withInitial(() -> false);

  public static Long id() {
    Long definida = DEFINIDA.get();
    if (definida != null) return definida;
    if (RESOLVENDO.get()) return RAIZ;
    RESOLVENDO.set(true);
    try {
      var auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal p && p.getLojaId() != null) {
        return p.getLojaId();
      }
      return RAIZ;
    } finally {
      RESOLVENDO.set(false);
    }
  }

  public static boolean raiz() {
    return RAIZ.equals(id());
  }

  /** Loja obrigatória (operações de usuário logado). */
  public static Long exigida() {
    Long id = id();
    if (RAIZ.equals(id)) throw new AccessDeniedException("Operação exige uma loja.");
    return id;
  }

  /** Executa [acao] como se estivesse na loja informada (abre sessões do Hibernate nessa loja). */
  public static <T> T comLoja(Long loja, Supplier<T> acao) {
    Long anterior = DEFINIDA.get();
    DEFINIDA.set(loja);
    try {
      return acao.get();
    } finally {
      if (anterior == null) DEFINIDA.remove();
      else DEFINIDA.set(anterior);
    }
  }
}
