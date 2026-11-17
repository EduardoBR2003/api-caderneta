package br.com.api_caderneta.auth;

import br.com.api_caderneta.audit.AuditoriaService;
import br.com.api_caderneta.auth.Dtos.*;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ConflictException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.model.ConviteCliente;
import br.com.api_caderneta.model.Usuario;
import br.com.api_caderneta.model.enums.Perfil;
import br.com.api_caderneta.repository.*;
import br.com.api_caderneta.security.UsuarioPrincipal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConvitesClienteService {
  private static final Logger log = LoggerFactory.getLogger(ConvitesClienteService.class);
  private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final int TAMANHO_CODIGO = 20;
  private static final String CONVITE_INVALIDO = "Convite inválido ou expirado.";
  private final SecureRandom random = new SecureRandom();
  private final UsuarioRepository usuarios;
  private final ConviteClienteRepository convites;
  private final ClienteRepository clientes;
  private final PessoaRepository pessoas;
  private final PasswordEncoder encoder;
  private final AuditoriaService auditoria;
  private final br.com.api_caderneta.lojas.LojaRepository lojas;
  private final long validadeConviteDias;

  public ConvitesClienteService(
      UsuarioRepository usuarios,
      ConviteClienteRepository convites,
      ClienteRepository clientes,
      PessoaRepository pessoas,
      PasswordEncoder encoder,
      AuditoriaService auditoria,
      br.com.api_caderneta.lojas.LojaRepository lojas,
      @Value("${caderneta.convite.validade-dias:7}") long validadeConviteDias) {
    this.usuarios = usuarios;
    this.convites = convites;
    this.clientes = clientes;
    this.pessoas = pessoas;
    this.encoder = encoder;
    this.auditoria = auditoria;
    this.lojas = lojas;
    this.validadeConviteDias = validadeConviteDias;
  }

  @Transactional
  public ConviteDTO gerarConvite(Long clienteId, UsuarioPrincipal autor) {
    clientes
        .bloquear(clienteId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + clienteId))
        .exigirAtivo();
    if (usuarios.existsByPessoaId(clienteId)) {
      throw new ConflictException("Este cliente já possui acesso ativado.");
    }
    StringBuilder codigo = new StringBuilder(TAMANHO_CODIGO);
    for (int i = 0; i < TAMANHO_CODIGO; i++)
      codigo.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
    Instant expira = Instant.now().plus(Duration.ofDays(validadeConviteDias));
    var convite =
        convites.save(
            new ConviteCliente(clienteId, hash(codigo.toString()), expira, autor.getId()));
    // Nunca registrar o código; apenas o vínculo e a validade.
    auditoria.registrar(
        "CONVITE_GERADO",
        "CONVITE",
        convite.getId(),
        AuditoriaService.mudanca("clienteId", null, clienteId, "expiraEm", null, expira));
    log.info("Convite gerado para o cliente {} pelo usuário {}", clienteId, autor.getId());
    return new ConviteDTO(codigo.toString(), expira);
  }

  /**
   * Consome o convite e cria a conta na mesma transação; o convite é travado contra uso
   * concorrente.
   */
  @Transactional
  public void registrarCliente(RegistroClienteRequest req) {
    var convite =
        convites
            .findByCodigoHash(hash(req.codigoConvite()))
            .orElseThrow(() -> new BusinessException(CONVITE_INVALIDO));
    if (convite.getUsadoEm() != null || !convite.getExpiraEm().isAfter(Instant.now())) {
      throw new BusinessException(CONVITE_INVALIDO);
    }
    var pessoa =
        pessoas
            .findById(convite.getClienteId())
            .orElseThrow(() -> new BusinessException(CONVITE_INVALIDO));
    if (pessoa instanceof br.com.api_caderneta.model.Cliente c) c.exigirAtivo();
    if (!lojas.findById(pessoa.getLojaId()).orElseThrow().isEmailConfirmado())
      throw new BusinessException("A loja precisa confirmar o e-mail antes de ativar clientes.");
    if (!soDigitos(pessoa.getCpf()).equals(soDigitos(req.cpf()))) {
      throw new BusinessException(CONVITE_INVALIDO); // mesma mensagem: não revela qual dado falhou
    }
    if (usuarios.existsByPessoaId(pessoa.getId())) {
      throw new ConflictException("Este cliente já possui acesso ativado.");
    }
    String login = req.login().trim();
    if (usuarios.existsByLogin(login)) throw new ConflictException("Login já em uso.");
    var conta =
        usuarios.save(
            new Usuario(
                pessoa.getLojaId(),
                pessoa.getId(),
                login,
                encoder.encode(req.senha()),
                Perfil.CLIENTE));
    convite.setUsadoEm(Instant.now());
    auditoria.registrarComo(
        pessoa.getLojaId(),
        conta.getId(),
        login,
        Perfil.CLIENTE.name(),
        "ACESSO_ATIVADO_POR_CONVITE",
        "USUARIO",
        conta.getId(),
        AuditoriaService.mudanca(
            "convite", null, convite.getId(), "pessoaId", null, pessoa.getId()));
    log.info("Acesso de cliente ativado. pessoaId={}", pessoa.getId());
  }

  static String hash(String codigo) {
    String normalizado = codigo.replaceAll("[\\s-]", "").toUpperCase();
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256")
                  .digest(normalizado.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String soDigitos(String s) {
    return s == null ? "" : s.replaceAll("\\D", "");
  }
}
