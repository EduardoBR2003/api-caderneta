package br.com.api_caderneta.auth;

import br.com.api_caderneta.model.enums.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

/** DTOs do contrato de autenticação e contas. Nenhum carrega hash, token ou dados de sessão. */
public final class Dtos {
    private Dtos() {}

    public static final String LOGIN_REGEX = "^[A-Za-z0-9._-]{3,50}$";

    public record LoginRequest(@NotBlank String login, @NotBlank String senha) {}

    public record UsuarioDTO(Long id, Long pessoaId, String nome, Perfil perfil) {}

    public record SessaoDTO(UsuarioDTO usuario) {}

    public record RegistroClienteRequest(
            @NotBlank String cpf,
            @NotBlank String codigoConvite,
            @NotBlank @Pattern(regexp = LOGIN_REGEX, message = "use 3 a 50 caracteres: letras, números, ponto, hífen ou sublinhado")
            String login,
            @NotBlank @Size(min = 8, max = 72, message = "deve ter de 8 a 72 caracteres") String senha) {}

    public record ConviteDTO(String codigo, Instant expiraEm) {}

    /** Perfil não é aceito: a conta criada é sempre FUNCIONARIO. */
    public record FuncionarioContaRequest(
            @NotBlank @Size(max = 150) String nome,
            @NotBlank @Size(max = 14) String cpf,
            @Email @Size(max = 100) String email,
            @Size(max = 20) String telefone,
            @Size(max = 200) String endereco,
            @Size(max = 50) String matricula,
            @NotBlank @Size(max = 50) String cargo,
            @NotBlank @Pattern(regexp = LOGIN_REGEX, message = "use 3 a 50 caracteres: letras, números, ponto, hífen ou sublinhado")
            String login,
            @NotBlank @Size(min = 8, max = 72, message = "deve ter de 8 a 72 caracteres") String senha) {}

    public record ContaCriadaDTO(Long id, Long pessoaId, String nome, String login, Perfil perfil, boolean ativo) {}

    public record AtivoRequest(@NotNull Boolean ativo) {}

    /** Promoção/rebaixamento de conta da equipe: só GERENTE ou FUNCIONARIO. */
    public record PerfilRequest(@NotNull Perfil perfil) {}

    public record MensagemDTO(String message) {}

    /** Envelope paginado comum a todos os endpoints de listagem. */
    public record Pagina<T>(List<T> content, int number, int size, long totalElements, int totalPages,
                            boolean first, boolean last) {
        public static <T> Pagina<T> de(Page<T> p) {
            return new Pagina<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(),
                    p.getTotalPages(), p.isFirst(), p.isLast());
        }
    }
}
