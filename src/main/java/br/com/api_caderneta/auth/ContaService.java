package br.com.api_caderneta.auth;

import br.com.api_caderneta.audit.AuditoriaService;
import br.com.api_caderneta.auth.Dtos.*;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ConflictException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.model.ConviteCliente;
import br.com.api_caderneta.model.Funcionario;
import br.com.api_caderneta.model.Usuario;
import br.com.api_caderneta.model.enums.Perfil;
import br.com.api_caderneta.repository.*;
import br.com.api_caderneta.security.UsuarioPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class ContaService {

    private static final Logger log = LoggerFactory.getLogger(ContaService.class);
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sem 0/O/1/I
    private static final int TAMANHO_CODIGO = 20; // 20 x 5 bits = 100 bits
    public static final String SENHA_LEGADA = "{migrado-para-usuarios}";
    private static final String CONVITE_INVALIDO = "Convite inválido ou expirado.";

    private final UsuarioRepository usuarios;
    private final ConviteClienteRepository convites;
    private final ClienteRepository clientes;
    private final PessoaRepository pessoas;
    private final FuncionarioRepository funcionarios;
    private final PasswordEncoder encoder;
    private final FindByIndexNameSessionRepository<?> sessoes;
    private final AuditoriaService auditoria;
    private final long validadeConviteDias;
    private final SecureRandom random = new SecureRandom();

    public ContaService(UsuarioRepository usuarios, ConviteClienteRepository convites, ClienteRepository clientes,
                        PessoaRepository pessoas, FuncionarioRepository funcionarios, PasswordEncoder encoder,
                        FindByIndexNameSessionRepository<?> sessoes, AuditoriaService auditoria,
                        @Value("${caderneta.convite.validade-dias:7}") long validadeConviteDias) {
        this.usuarios = usuarios;
        this.convites = convites;
        this.clientes = clientes;
        this.pessoas = pessoas;
        this.funcionarios = funcionarios;
        this.encoder = encoder;
        this.sessoes = sessoes;
        this.auditoria = auditoria;
        this.validadeConviteDias = validadeConviteDias;
    }

    @Transactional(readOnly = true)
    public UsuarioDTO dadosDe(UsuarioPrincipal p) {
        var u = usuarios.findById(p.getId()).filter(Usuario::isAtivo).orElse(null);
        if (u == null) return null;
        String nome = usuarios.nomeDaPessoa(u.getPessoaId()).orElse(u.getLogin());
        return new UsuarioDTO(u.getId(), u.getPessoaId(), nome, u.getPerfil());
    }

    @Transactional
    public ConviteDTO gerarConvite(Long clienteId, UsuarioPrincipal autor) {
        if (!clientes.existsById(clienteId)) {
            throw new ResourceNotFoundException("Cliente não encontrado com o ID: " + clienteId);
        }
        if (usuarios.existsByPessoaId(clienteId)) {
            throw new ConflictException("Este cliente já possui acesso ativado.");
        }
        StringBuilder codigo = new StringBuilder(TAMANHO_CODIGO);
        for (int i = 0; i < TAMANHO_CODIGO; i++) codigo.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        Instant expira = Instant.now().plus(Duration.ofDays(validadeConviteDias));
        var convite = convites.save(new ConviteCliente(clienteId, hash(codigo.toString()), expira, autor.getId()));
        // Nunca registrar o código; apenas o vínculo e a validade.
        auditoria.registrar("CONVITE_GERADO", "CONVITE", convite.getId(), AuditoriaService.mudanca(
                "clienteId", null, clienteId, "expiraEm", null, expira));
        log.info("Convite gerado para o cliente {} pelo usuário {}", clienteId, autor.getId());
        return new ConviteDTO(codigo.toString(), expira);
    }

    /** Consome o convite e cria a conta na mesma transação; o convite é travado contra uso concorrente. */
    @Transactional
    public void registrarCliente(RegistroClienteRequest req) {
        var convite = convites.findByCodigoHash(hash(req.codigoConvite()))
                .orElseThrow(() -> new BusinessException(CONVITE_INVALIDO));
        if (convite.getUsadoEm() != null || !convite.getExpiraEm().isAfter(Instant.now())) {
            throw new BusinessException(CONVITE_INVALIDO);
        }
        var pessoa = pessoas.findById(convite.getClienteId())
                .orElseThrow(() -> new BusinessException(CONVITE_INVALIDO));
        if (!soDigitos(pessoa.getCpf()).equals(soDigitos(req.cpf()))) {
            throw new BusinessException(CONVITE_INVALIDO); // mesma mensagem: não revela qual dado falhou
        }
        if (usuarios.existsByPessoaId(pessoa.getId())) {
            throw new ConflictException("Este cliente já possui acesso ativado.");
        }
        String login = req.login().trim();
        if (usuarios.existsByLogin(login)) throw new ConflictException("Login já em uso.");
        var conta = usuarios.save(new Usuario(pessoa.getId(), login, encoder.encode(req.senha()), Perfil.CLIENTE));
        convite.setUsadoEm(Instant.now());
        auditoria.registrarComo(conta.getId(), login, Perfil.CLIENTE.name(), "ACESSO_ATIVADO_POR_CONVITE",
                "USUARIO", conta.getId(), AuditoriaService.mudanca("convite", null, convite.getId(),
                        "pessoaId", null, pessoa.getId()));
        log.info("Acesso de cliente ativado. pessoaId={}", pessoa.getId());
    }

    @Transactional
    public ContaCriadaDTO criarFuncionario(FuncionarioContaRequest req) {
        String login = req.login().trim();
        if (funcionarios.existsByCpf(req.cpf()) || pessoas.existsByCpf(req.cpf())) {
            throw new ConflictException("Já existe uma pessoa cadastrada com o CPF informado.");
        }
        if (usuarios.existsByLogin(login) || funcionarios.existsByLogin(login)) {
            throw new ConflictException("Login já em uso.");
        }
        if (req.matricula() != null && !req.matricula().isBlank() && funcionarios.existsByMatricula(req.matricula())) {
            throw new ConflictException("Matrícula já em uso.");
        }
        var f = funcionarios.save(new Funcionario(req.nome(), req.cpf(), req.endereco(), req.email(),
                req.telefone(), blankToNull(req.matricula()), req.cargo(), login, SENHA_LEGADA));
        var u = usuarios.save(new Usuario(f.getId(), login, encoder.encode(req.senha()), Perfil.FUNCIONARIO));
        auditoria.registrar("ACESSO_CRIADO", "USUARIO", u.getId(), AuditoriaService.mudanca(
                "login", null, u.getLogin(), "perfil", null, u.getPerfil()));
        return new ContaCriadaDTO(u.getId(), f.getId(), f.getNome(), u.getLogin(), u.getPerfil(), u.isAtivo());
    }

    @Transactional(readOnly = true)
    public Pagina<UsuarioRepository.UsuarioResumo> listar(int page, int size) {
        return Pagina.de(usuarios.listar(PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100))));
    }

    @Transactional
    public void alterarAtivo(Long id, boolean ativo) {
        var u = usuarios.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada com o ID: " + id));
        if (u.isAtivo() == ativo) return;
        if (!ativo && u.getPerfil() == Perfil.GERENTE && usuarios.countByPerfilAndAtivoTrue(Perfil.GERENTE) <= 1) {
            throw new ConflictException("Não é possível desativar o último gerente ativo.");
        }
        u.setAtivo(ativo);
        usuarios.save(u);
        auditoria.registrar(ativo ? "ACESSO_REATIVADO" : "ACESSO_DESATIVADO", "USUARIO", id,
                AuditoriaService.mudanca("ativo", !ativo, ativo));
        if (!ativo) encerrarSessoesAposCommit(u.getLogin());
    }

    private void encerrarSessoesAposCommit(String login) {
        Runnable encerrar = () -> {
            @SuppressWarnings("unchecked")
            var repo = (SessionRepository<?>) sessoes;
            sessoes.findByPrincipalName(login).keySet().forEach(repo::deleteById);
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { encerrar.run(); }
            });
        } else {
            encerrar.run();
        }
    }

    static String hash(String codigo) {
        String normalizado = codigo.replaceAll("[\\s-]", "").toUpperCase();
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(normalizado.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String soDigitos(String s) { return s == null ? "" : s.replaceAll("\\D", ""); }
    private static String blankToNull(String s) { return s == null || s.isBlank() ? null : s; }
}
