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

/** Promoção de funcionário a gerente (e o inverso) mantendo o mesmo login. */
class AlterarPerfilTest {

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
  void gerentePromoveFuncionarioMantendoLoginAuditaEEncerraSessoes() {
    var u = conta(5, 50, "func.maria", Perfil.FUNCIONARIO, true);

    service.alterarPerfil(5L, Perfil.GERENTE);

    assertEquals(Perfil.GERENTE, u.getPerfil());
    assertEquals("func.maria", u.getLogin(), "mesmo login");
    verify(usuarios).save(u);
    verify(auditoria).registrar(eq("PERFIL_ALTERADO"), eq("USUARIO"), eq(5L), any());
    verify(sessoes).findByPrincipalName("func.maria");
  }

  @Test
  void gerenteRebaixaOutroGerenteQuandoHaMaisDeUm() {
    var u = conta(6, 60, "ger.dois", Perfil.GERENTE, true);
    when(usuarios.countByLojaIdAndPerfilAndAtivoTrue(1L, Perfil.GERENTE)).thenReturn(2L);

    service.alterarPerfil(6L, Perfil.FUNCIONARIO);

    assertEquals(Perfil.FUNCIONARIO, u.getPerfil());
  }

  @Test
  void naoRebaixaOUltimoGerenteAtivo() {
    var u = conta(7, 70, "ger.unico", Perfil.GERENTE, true);
    when(usuarios.countByLojaIdAndPerfilAndAtivoTrue(1L, Perfil.GERENTE)).thenReturn(1L);

    assertThrows(ConflictException.class, () -> service.alterarPerfil(7L, Perfil.FUNCIONARIO));
    assertEquals(Perfil.GERENTE, u.getPerfil());
    verify(usuarios, never()).save(any());
  }

  @Test
  void funcionarioNaoPodeAlterarPerfis() {
    TestAuth.entrar(2, Perfil.FUNCIONARIO);
    conta(8, 80, "func.joao", Perfil.FUNCIONARIO, true);

    assertThrows(AccessDeniedException.class, () -> service.alterarPerfil(8L, Perfil.GERENTE));
    verify(usuarios, never()).save(any());
  }

  @Test
  void contaDeClienteNaoViraEquipe() {
    conta(9, 90, "cliente.ana", Perfil.CLIENTE, false);

    assertThrows(BusinessException.class, () -> service.alterarPerfil(9L, Perfil.GERENTE));
    verify(usuarios, never()).save(any());
  }

  @Test
  void perfilClienteNaoEhAceitoComoDestino() {
    conta(10, 100, "func.bia", Perfil.FUNCIONARIO, true);

    assertThrows(BusinessException.class, () -> service.alterarPerfil(10L, Perfil.CLIENTE));
  }

  @Test
  void mesmoPerfilNaoFazNada() {
    conta(11, 110, "func.caio", Perfil.FUNCIONARIO, true);

    service.alterarPerfil(11L, Perfil.FUNCIONARIO);

    verify(usuarios, never()).save(any());
    verify(auditoria, never()).registrar(any(), any(), any(), any());
  }
}
