package br.com.api_caderneta.auth;

import br.com.api_caderneta.audit.AuditoriaService;
import br.com.api_caderneta.auth.Dtos.RegistroClienteRequest;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ConflictException;
import br.com.api_caderneta.model.Cliente;
import br.com.api_caderneta.model.ConviteCliente;
import br.com.api_caderneta.model.Usuario;
import br.com.api_caderneta.model.enums.Perfil;
import br.com.api_caderneta.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContaServiceTest {

    UsuarioRepository usuarios = mock(UsuarioRepository.class);
    ConviteClienteRepository convites = mock(ConviteClienteRepository.class);
    PessoaRepository pessoas = mock(PessoaRepository.class);
    AuditoriaService auditoria = mock(AuditoriaService.class);
    ContaService service;

    @BeforeEach
    void init() {
        service = new ContaService(usuarios, convites, mock(ClienteRepository.class), pessoas,
                mock(FuncionarioRepository.class), new BCryptPasswordEncoder(),
                mock(FindByIndexNameSessionRepository.class), auditoria, 7);
    }

    private static Cliente cliente(long id, String cpf) {
        var c = new Cliente();
        c.setId(id);
        c.setCpf(cpf);
        return c;
    }

    private ConviteCliente conviteValido(String codigo) {
        var c = new ConviteCliente(3L, ContaService.hash(codigo), Instant.now().plus(Duration.ofDays(1)), 1L);
        when(convites.findByCodigoHash(ContaService.hash(codigo))).thenReturn(Optional.of(c));
        return c;
    }

    private static RegistroClienteRequest req(String cpf, String codigo) {
        return new RegistroClienteRequest(cpf, codigo, "cliente.um", "SenhaForte#1");
    }

    @Test
    void hashIgnoraCaixaEspacosEHifens() {
        assertEquals(ContaService.hash("ABCD2345"), ContaService.hash(" abcd-2345 "));
        assertNotEquals(ContaService.hash("ABCD2345"), ContaService.hash("ABCD2346"));
        assertEquals(64, ContaService.hash("x").length());
    }

    @Test
    void ativacaoValidaCriaContaClienteComHashEMarcaConviteUsado() {
        var convite = conviteValido("CODIGO1");
        when(pessoas.findById(3L)).thenReturn(Optional.of(cliente(3, "333.333.333-33")));
        when(usuarios.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        service.registrarCliente(req("33333333333", "CODIGO1"));

        var cap = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(cap.capture());
        assertEquals(Perfil.CLIENTE, cap.getValue().getPerfil());
        assertEquals(3L, cap.getValue().getPessoaId());
        assertNotEquals("SenhaForte#1", cap.getValue().getSenhaHash());
        assertTrue(new BCryptPasswordEncoder().matches("SenhaForte#1", cap.getValue().getSenhaHash()));
        assertNotNull(convite.getUsadoEm());
    }

    @Test
    void conviteInexistenteUsadoOuExpiradoERecusadoComAMesmaMensagem() {
        when(convites.findByCodigoHash(any())).thenReturn(Optional.empty());
        var e1 = assertThrows(BusinessException.class, () -> service.registrarCliente(req("33333333333", "X")));

        var usado = conviteValido("USADO");
        usado.setUsadoEm(Instant.now());
        var e2 = assertThrows(BusinessException.class, () -> service.registrarCliente(req("33333333333", "USADO")));

        when(convites.findByCodigoHash(ContaService.hash("VELHO"))).thenReturn(Optional.of(
                new ConviteCliente(3L, ContaService.hash("VELHO"), Instant.now().minusSeconds(1), 1L)));
        var e3 = assertThrows(BusinessException.class, () -> service.registrarCliente(req("33333333333", "VELHO")));

        assertEquals(e1.getMessage(), e2.getMessage());
        assertEquals(e1.getMessage(), e3.getMessage());
        verify(usuarios, never()).save(any());
    }

    @Test
    void cpfDiferenteNaoCriaAcessoENaoConsomeOConvite() {
        var convite = conviteValido("CODIGO2");
        when(pessoas.findById(3L)).thenReturn(Optional.of(cliente(3, "333.333.333-33")));
        assertThrows(BusinessException.class, () -> service.registrarCliente(req("000.000.000-00", "CODIGO2")));
        verify(usuarios, never()).save(any());
        assertNull(convite.getUsadoEm());
    }

    @Test
    void loginEmUsoEClienteJaAtivadoSaoConflito() {
        conviteValido("CODIGO3");
        when(pessoas.findById(3L)).thenReturn(Optional.of(cliente(3, "333.333.333-33")));
        when(usuarios.existsByLogin("cliente.um")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.registrarCliente(req("33333333333", "CODIGO3")));

        when(usuarios.existsByLogin("cliente.um")).thenReturn(false);
        when(usuarios.existsByPessoaId(3L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.registrarCliente(req("33333333333", "CODIGO3")));
    }

    @Test
    void naoDesativaOUltimoGerenteAtivo() {
        var gerente = new Usuario(1L, "g", "x", Perfil.GERENTE);
        when(usuarios.findById(10L)).thenReturn(Optional.of(gerente));
        when(usuarios.countByPerfilAndAtivoTrue(Perfil.GERENTE)).thenReturn(1L);
        assertThrows(ConflictException.class, () -> service.alterarAtivo(10L, false));
        assertTrue(gerente.isAtivo());
        verify(usuarios, never()).save(any());
    }
}
