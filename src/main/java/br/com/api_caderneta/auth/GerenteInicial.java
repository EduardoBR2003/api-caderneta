package br.com.api_caderneta.auth;

import br.com.api_caderneta.model.Proprietario;
import br.com.api_caderneta.model.Usuario;
import br.com.api_caderneta.model.enums.Perfil;
import br.com.api_caderneta.repository.PessoaRepository;
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
    private final PessoaRepository pessoas;
    private final PasswordEncoder encoder;
    private final TransactionTemplate tx;
    private final String login, senha, nome, cpf;

    public GerenteInicial(UsuarioRepository usuarios, PessoaRepository pessoas, PasswordEncoder encoder,
                          TransactionTemplate tx,
                          @Value("${caderneta.bootstrap.login:}") String login,
                          @Value("${caderneta.bootstrap.senha:}") String senha,
                          @Value("${caderneta.bootstrap.nome:Gerente}") String nome,
                          @Value("${caderneta.bootstrap.cpf:}") String cpf) {
        this.usuarios = usuarios;
        this.pessoas = pessoas;
        this.encoder = encoder;
        this.tx = tx;
        this.login = login;
        this.senha = senha;
        this.nome = nome;
        this.cpf = cpf;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarios.countByPerfilAndAtivoTrue(Perfil.GERENTE) > 0) return;
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
            if (usuarios.existsByLogin(login) || pessoas.existsByCpf(cpf)) {
                log.error("Gerente inicial NÃO criado: login ou CPF já existe. Reative a conta pelo banco ou use outros valores.");
                return;
            }
            var p = pessoas.save(new Proprietario(nome, cpf, null, null, null, null, "Gerente", login,
                    ContaService.SENHA_LEGADA));
            usuarios.save(new Usuario(p.getId(), login, encoder.encode(senha), Perfil.GERENTE));
            log.info("Gerente inicial criado (login '{}').", login);
        });
    }
}
