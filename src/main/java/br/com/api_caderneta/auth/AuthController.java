package br.com.api_caderneta.auth;

import br.com.api_caderneta.auth.Dtos.*;
import br.com.api_caderneta.exceptions.ExceptionResponse;
import br.com.api_caderneta.security.UsuarioPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final SessionAuthenticationStrategy sessionStrategy;
    private final SecurityContextRepository contextRepository;
    private final ContaService contas;
    private final LimiteTentativas limite;

    @org.springframework.beans.factory.annotation.Autowired
    private br.com.api_caderneta.lojas.LojaService lojasService;

    public AuthController(AuthenticationManager authManager, SessionAuthenticationStrategy sessionStrategy,
                          SecurityContextRepository contextRepository, ContaService contas,
                          LimiteTentativas limite) {
        this.authManager = authManager;
        this.sessionStrategy = sessionStrategy;
        this.contextRepository = contextRepository;
        this.contas = contas;
        this.limite = limite;
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrf(CsrfToken token) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(Map.of("token", token.getToken(), "headerName", token.getHeaderName()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req,
                                   HttpServletRequest request, HttpServletResponse response) {
        String ip = request.getRemoteAddr();
        // Bloqueio temporário por login e por IP (429), verificado antes do BCrypt.
        limite.verificar(LimiteTentativas.chaveLogin(req.login()), LimiteTentativas.chaveIp(ip));
        Authentication auth;
        try {
            auth = authManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(req.login().trim(), req.senha()));
        } catch (AuthenticationException e) {
            limite.registrarFalhaLogin(req.login(), ip);
            // Mesma resposta para conta inexistente, senha errada e conta inativa.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ExceptionResponse(
                    "CREDENCIAIS_INVALIDAS", "Login ou senha inválidos.", new Date(), null));
        }
        var dados = contas.dadosDe((UsuarioPrincipal) auth.getPrincipal());
        if (dados == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ExceptionResponse(
                    "CREDENCIAIS_INVALIDAS", "Login ou senha inválidos.", new Date(), null));
        }
        limite.registrarSucesso(req.login());
        // Renova o id da sessão e o token CSRF (proteção contra fixação de sessão).
        sessionStrategy.onAuthentication(auth, request, response);
        SecurityContext context = SecurityContextHolder.getContextHolderStrategy().createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.getContextHolderStrategy().setContext(context);
        contextRepository.saveContext(context, request, response);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new SessaoDTO(dados));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UsuarioPrincipal principal,
                                HttpServletRequest request, HttpServletResponse response) {
        var dados = contas.dadosDe(principal);
        if (dados == null) { // conta removida ou desativada: a sessão não vale mais
            new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ExceptionResponse(
                    "NAO_AUTENTICADO", "Sua sessão expirou. Entre novamente.", new Date(), null));
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new SessaoDTO(dados));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) {
        // Invalida a sessão no servidor; o Spring Session expira o cookie SESSION com os mesmos atributos.
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    /** Cadastro público de loja: cria a loja e o dono (gerente). Limitado por IP. */
    @PostMapping("/lojas")
    public ResponseEntity<LojaCriadaDTO> criarLoja(@Valid @RequestBody CriarLojaRequest req,
                                                   HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        limite.verificar(LimiteTentativas.chaveCadastroLoja(ip));
        limite.registrarCadastroLoja(ip); // conta toda tentativa, com sucesso ou não
        return ResponseEntity.status(HttpStatus.CREATED).body(lojasService.criar(req));
    }

    @PostMapping("/registro-cliente")
    public ResponseEntity<MensagemDTO> registro(@Valid @RequestBody RegistroClienteRequest req,
                                                HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        limite.verificar(LimiteTentativas.chaveIp(ip));
        try {
            contas.registrarCliente(req);
        } catch (RuntimeException e) {
            limite.registrarFalhaIp(ip); // código de convite inválido/expirado etc.: freia tentativa e erro
            throw e;
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensagemDTO("Acesso criado. Entre com seu login e senha."));
    }

    @PostMapping("/funcionarios")
    public ResponseEntity<ContaCriadaDTO> funcionario(@Valid @RequestBody FuncionarioContaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contas.criarFuncionario(req));
    }
}
