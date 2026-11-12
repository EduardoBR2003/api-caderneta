package br.com.api_caderneta.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import br.com.api_caderneta.TestAuth;
import br.com.api_caderneta.audit.AuditoriaService;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ConflictException;
import br.com.api_caderneta.model.Usuario;
import br.com.api_caderneta.model.enums.Perfil;
import br.com.api_caderneta.repository.*;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.test.util.ReflectionTestUtils;

/** Exclusão lógica e restauração de contas da equipe. */
class ExclusaoContaTest {

  UsuarioRepository usuarios = mock(UsuarioRepository.class);
  FuncionarioRepository funcionarios = mock(FuncionarioRepository.class);
  FindByIndexNameSessionRepository<?> sessoes = mock(FindByIndexNameSessionRepository.class);
  AuditoriaService auditoria = mock(AuditoriaService.class);
  ContaService service;

  @BeforeEach
  void init() {
    service =
        new ContaService(
            usuarios,
            mock(ConviteClienteRepository.class),
            mock(ClienteRepository.class),
            mock(PessoaRepository.class),
            funcionarios,
            new BCryptPasswordEncoder(),
            sessoes,
            auditoria,
            7);
    TestAuth.entrar(1, Perfil.GERENTE);
  }

  @AfterEach
  void sair() {
    SecurityContextHolder.clearContext();
  }

  private Usuario conta(long id, long pessoa, String login, Perfil perfil, boolean equipe) {
    var u = new Usuario(pessoa, login, "hash", perfil);
    ReflectionTestUtils.setField(u, "id", id);
    when(usuarios.findById(id)).thenReturn(Optional.of(u));
    when(funcionarios.existsById(pessoa)).thenReturn(equipe);
    return u;
  }

  @Test
  void excluiEscondeDesativaAuditaEEncerraSessoes() {
    var u = conta(5, 50, "func.maria", Perfil.FUNCIONARIO, true);

    service.excluir(5L);

    assertTrue(u.isExcluido());
    assertFalse(u.isAtivo());
    verify(usuarios).save(u);
    verify(auditoria).registrar(eq("ACESSO_EXCLUIDO"), eq("USUARIO"), eq(5L), any());
    verify(sessoes).findByPrincipalName("func.maria");
  }

  @Test
  void naoExcluiAPropriaConta() {
    conta(1, 10, "ger.atual", Perfil.GERENTE, true);
    assertThrows(BusinessException.class, () -> service.excluir(1L));
  }

  @Test
  void naoExcluiOUltimoGerenteAtivo() {
    conta(7, 70, "ger.unico", Perfil.GERENTE, true);
    when(usuarios.countByLojaIdAndPerfilAndAtivoTrue(1L, Perfil.GERENTE)).thenReturn(1L);
    assertThrows(ConflictException.class, () -> service.excluir(7L));
    verify(usuarios, never()).save(any());
  }

  @Test
  void contaDeClienteNaoEhExcluidaPorAqui() {
    conta(9, 90, "cliente.ana", Perfil.CLIENTE, false);
    assertThrows(BusinessException.class, () -> service.excluir(9L));
  }

  @Test
  void funcionarioNaoExclui() {
    TestAuth.entrar(2, Perfil.FUNCIONARIO);
    conta(8, 80, "func.joao", Perfil.FUNCIONARIO, true);
    assertThrows(AccessDeniedException.class, () -> service.excluir(8L));
  }

  @Test
  void restaurarExigeContaExcluidaETrocaASenhaEVoltaComoFuncionario() {
    var u = conta(6, 60, "ger.antigo", Perfil.GERENTE, true);
    assertThrows(BusinessException.class, () -> service.restaurar(6L, "Senha-Nova-1"));

    u.setExcluidoEm(java.time.Instant.now());
    u.setAtivo(false);
    service.restaurar(6L, "Senha-Nova-1");

    assertFalse(u.isExcluido());
    assertTrue(u.isAtivo());
    assertEquals(Perfil.FUNCIONARIO, u.getPerfil());
    assertTrue(new BCryptPasswordEncoder().matches("Senha-Nova-1", u.getSenhaHash()));
    verify(auditoria).registrar(eq("ACESSO_RESTAURADO"), eq("USUARIO"), eq(6L), any());
  }

  @Test
  void contaExcluidaNaoEhAtivadaNemPromovida() {
    var u = conta(4, 40, "func.saiu", Perfil.FUNCIONARIO, true);
    u.setExcluidoEm(java.time.Instant.now());
    assertThrows(BusinessException.class, () -> service.alterarAtivo(4L, true));
    assertThrows(BusinessException.class, () -> service.alterarPerfil(4L, Perfil.GERENTE));
  }
}
