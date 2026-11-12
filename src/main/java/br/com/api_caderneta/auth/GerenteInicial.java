package br.com.api_caderneta.auth;

import br.com.api_caderneta.model.Proprietario;
import br.com.api_caderneta.model.Usuario;
import br.com.api_caderneta.model.enums.Perfil;
import br.com.api_caderneta.repository.FuncionarioRepository;
import br.com.api_caderneta.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Cria o primeiro gerente a partir de variáveis de ambiente. Idempotente: se já existe gerente ativo,
 * não faz nada. Não há senha padrão: sem as variáveis, apenas avisa no log.
 */
@Component
public class GerenteInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(GerenteInicial.class);

    private final UsuarioRepository usuarios;
    private final FuncionarioRepository funcionarios;
    private final PasswordEncoder encoder;
    private final TransactionTemplate tx;
    private final String login, senha, nome, cpf;

    public GerenteInicial(UsuarioRepository usuarios, FuncionarioRepository funcionarios, PasswordEncoder encoder,
                          TransactionTemplate tx,
                          @Value("${caderneta.bootstrap.login:}") String login,
                          @Value("${caderneta.bootstrap.senha:}") String senha,
                          @Value("${caderneta.bootstrap.nome:Gerente}") String nome,
                          @Value("${caderneta.bootstrap.cpf:}") String cpf) {
        this.usuarios = usuarios;
        this.funcionarios = funcionarios;
        this.encoder = encoder;
        this.tx = tx;
        this.login = login;
        this.senha = senha;
        this.nome = nome;
        this.cpf = cpf;
    }

    /** Loja dos dados anteriores às lojas (criada pela V12). */
    public static final Long LOJA_PRINCIPAL = 1L;

    @Override
    public void run(ApplicationArguments args) {
        // O gerente inicial pertence à Loja Principal; as demais lojas nascem pelo cadastro público.
        br.com.api_caderneta.lojas.LojaAtual.comLoja(LOJA_PRINCIPAL, () -> {
            criarSeNecessario();
            return null;
        });
    }

    private void criarSeNecessario() {
        if (usuarios.countByLojaIdAndPerfilAndAtivoTrue(LOJA_PRINCIPAL, Perfil.GERENTE) > 0) return;
        if (login.isBlank() || senha.isBlank() || cpf.isBlank()) {
            log.warn("Não há gerente ativo e CADERNETA_BOOTSTRAP_LOGIN/SENHA/CPF não foram informados: "
                    + "ninguém conseguirá administrar o sistema.");
            return;
        }
        if (senha.length() < 8 || senha.length() > 72 || !login.matches(Dtos.LOGIN_REGEX)) {
            log.error("Gerente inicial NÃO criado: login (3-50: letras, números, . _ -) ou senha (8-72) inválidos.");
            return;
        }
        tx.executeWithoutResult(s -> {
            // CPF único por papel: só a equipe conflita (o mesmo CPF pode ser de um cliente).
            if (usuarios.existsByLogin(login) || funcionarios.existsByCpf(cpf)) {
                log.error("Gerente inicial NÃO criado: login ou CPF já existe. Reative a conta pelo banco ou use outros valores.");
                return;
            }
            var p = funcionarios.save(new Proprietario(nome, cpf, null, null, null, null, "Gerente", login,
                    ContaService.SENHA_LEGADA));
            usuarios.save(new Usuario(p.getId(), login, encoder.encode(senha), Perfil.GERENTE));
            log.info("Gerente inicial criado (login '{}').", login);
        });
    }
}
