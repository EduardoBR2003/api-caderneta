package br.com.api_caderneta.historico;

import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.model.Usuario;
import br.com.api_caderneta.model.enums.Perfil;
import br.com.api_caderneta.security.UsuarioPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;

/** As validações ocorrem antes de qualquer consulta, então o EntityManager não é necessário. */
class HistoricoServiceTest {

    private final HistoricoService service = new HistoricoService(null);

    private static UsuarioPrincipal usuario(Perfil perfil) {
        return new UsuarioPrincipal(new Usuario(1L, "u", "x", perfil));
    }

    @Test
    void funcionarioNaoPodePedirTipoForaDosTresOperacionais() {
        assertThrows(AccessDeniedException.class,
                () -> service.geral(usuario(Perfil.FUNCIONARIO), 0, 20, "LEMBRETE_PAGAMENTO", null, null));
        assertThrows(AccessDeniedException.class,
                () -> service.geral(usuario(Perfil.FUNCIONARIO), 0, 20, "CONFIRMACAO_COMPRA", null, null));
    }

    @Test
    void tipoInexistenteEhErroDeValidacaoParaQualquerPerfil() {
        assertThrows(BusinessException.class, () -> service.geral(usuario(Perfil.FUNCIONARIO), 0, 20, "XYZ", null, null));
        assertThrows(BusinessException.class, () -> service.geral(usuario(Perfil.GERENTE), 0, 20, "XYZ", null, null));
        assertThrows(BusinessException.class, () -> service.proprio(usuario(Perfil.CLIENTE), 0, 20, "XYZ", null, null));
    }

    @Test
    void recorteDoFuncionarioTemExatamenteTresTipos() {
        assertEquals(3, HistoricoService.TIPOS_FUNCIONARIO.size());
    }

    @Test
    void paginacaoInvalidaEhRejeitada() {
        var g = usuario(Perfil.GERENTE);
        assertThrows(BusinessException.class, () -> service.geral(g, -1, 20, null, null, null));
        assertThrows(BusinessException.class, () -> service.geral(g, 0, 0, null, null, null));
        assertThrows(BusinessException.class, () -> service.geral(g, 0, 101, null, null, null));
    }
}
