package br.com.api_caderneta.lojas;

import br.com.api_caderneta.audit.AuditoriaService;
import br.com.api_caderneta.auth.ContaService;
import br.com.api_caderneta.auth.Dtos.CriarLojaRequest;
import br.com.api_caderneta.auth.Dtos.LojaCriadaDTO;
import br.com.api_caderneta.exceptions.ConflictException;
import br.com.api_caderneta.model.Proprietario;
import br.com.api_caderneta.model.Usuario;
import br.com.api_caderneta.model.enums.Perfil;
import br.com.api_caderneta.repository.FuncionarioRepository;
import br.com.api_caderneta.repository.UsuarioRepository;
import br.com.api_caderneta.util.Regras;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Cadastro público de loja. Em duas etapas porque a sessão do Hibernate guarda a loja ao abrir:
 * (1) cria a loja; (2) já "dentro" dela, cria o dono (Proprietario + conta GERENTE). Se (2) falhar,
 * a loja recém-criada é removida.
 */
@Service
public class LojaService {

  private static final Logger log = LoggerFactory.getLogger(LojaService.class);

  private final LojaRepository lojas;
  private final UsuarioRepository usuarios;
  private final FuncionarioRepository funcionarios;
  private final PasswordEncoder encoder;
  private final AuditoriaService auditoria;
  private final TransactionTemplate tx;

  public LojaService(
      LojaRepository lojas,
      UsuarioRepository usuarios,
      FuncionarioRepository funcionarios,
      PasswordEncoder encoder,
      AuditoriaService auditoria,
      PlatformTransactionManager transacoes) {
    this.lojas = lojas;
    this.usuarios = usuarios;
    this.funcionarios = funcionarios;
    this.encoder = encoder;
    this.auditoria = auditoria;
    this.tx = new TransactionTemplate(transacoes);
    this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  public LojaCriadaDTO criar(CriarLojaRequest req) {
    String login = req.login().trim();
    String nomeLoja = req.nomeLoja().trim();
    String nome = Regras.nome(req.nome());
    String cpf = Regras.cpf(req.cpf());
    // Login é global: nenhuma outra conta, de nenhuma loja, pode usá-lo.
    if (LojaAtual.comLoja(LojaAtual.RAIZ, () -> usuarios.existsByLogin(login) || funcionarios.existsByLogin(login))) {
      throw new ConflictException("Login já em uso.");
    }
    Loja loja = tx.execute(s -> lojas.save(new Loja(nomeLoja)));
    try {
      return LojaAtual.comLoja(
          loja.getId(),
          () ->
              tx.execute(
                  s -> {
                    var dono =
                        funcionarios.save(
                            new Proprietario(
                                nome, cpf, null, req.email(), req.telefone(), null, "Proprietário",
                                login, ContaService.SENHA_LEGADA));
                    var conta =
                        usuarios.save(
                            new Usuario(
                                loja.getId(), dono.getId(), login, encoder.encode(req.senha()),
                                Perfil.GERENTE));
                    auditoria.registrarComo(
                        loja.getId(), conta.getId(), login, Perfil.GERENTE.name(), "LOJA_CRIADA",
                        "LOJA", loja.getId(), AuditoriaService.mudanca("nome", null, nomeLoja));
                    log.info("Loja criada. lojaId={}", loja.getId());
                    return new LojaCriadaDTO(loja.getId(), loja.getNome(), login);
                  }));
    } catch (RuntimeException e) {
      tx.executeWithoutResult(s -> lojas.deleteById(loja.getId()));
      throw e;
    }
  }
}
