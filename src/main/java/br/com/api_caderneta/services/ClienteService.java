package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.*;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.Cliente;
import br.com.api_caderneta.model.enums.TipoNotificacao; // Import necessário
import br.com.api_caderneta.repository.ClienteRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

  private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);
  private final ClienteRepository repository;
  private final DataMapper mapper;
  private final ConsultaClientes consultas;

  // Registra eventos de histórico na mesma operação de negócio.
  private final NotificacaoService notificacaoService;

  private final br.com.api_caderneta.audit.AuditoriaService auditoria;
  private final FiadorDoCliente fiadores;
  private final br.com.api_caderneta.lojas.LojaRepository lojas;

  private void gerente() {
    if (br.com.api_caderneta.security.UsuarioAtual.obrigatorio().getPerfil()
        != br.com.api_caderneta.model.enums.Perfil.GERENTE)
      throw new org.springframework.security.access.AccessDeniedException(
          "Somente gerente pode alterar limite e prazo.");
  }

  @Transactional(readOnly = true)
  public br.com.api_caderneta.auth.Dtos.Pagina<ClienteResponseDTO> listar(
      int page, int size, String q) {
    return consultas.listar(page, size, q);
  }

  @Autowired
  public ClienteService(
      ClienteRepository repository,
      DataMapper mapper,
      NotificacaoService notificacaoService,
      ConsultaClientes consultas,
      br.com.api_caderneta.audit.AuditoriaService auditoria,
      FiadorDoCliente fiadores,
      br.com.api_caderneta.lojas.LojaRepository lojas) {
    this.auditoria = auditoria;
    this.fiadores = fiadores;
    this.lojas = lojas;

    this.repository = repository;
    this.mapper = mapper;
    this.consultas = consultas;
    this.notificacaoService = notificacaoService;
  }

  @Transactional(readOnly = true)
  public ClienteResponseDTO getClienteById(Long id) {
    logger.info("Buscando cliente com ID: {}", id);
    var entity =
        repository
            .findById(id)
            .orElseThrow(
                () -> {
                  logger.error("Cliente não encontrado com o ID: {}", id);
                  return new ResourceNotFoundException("Cliente não encontrado com o ID: " + id);
                });
    return mapper.parseObject(entity, ClienteResponseDTO.class);
  }

  @Transactional(readOnly = true)
  public List<ClienteResponseDTO> getAllClientes() {
    logger.info("Buscando todos os clientes");
    var entities = repository.findAll();
    return mapper.parseListObjects(entities, ClienteResponseDTO.class);
  }

  @Transactional
  public ClienteDTO createCliente(ClienteRequestDTO dto) {
    dto.setCpf(br.com.api_caderneta.util.Regras.cpf(dto.getCpf()));
    dto.setNome(br.com.api_caderneta.util.Regras.nome(dto.getNome()));
    boolean gerente =
        br.com.api_caderneta.security.UsuarioAtual.obrigatorio().getPerfil()
            == br.com.api_caderneta.model.enums.Perfil.GERENTE;
    if (!gerente && (dto.temLimite() || dto.temPrazo())) gerente();
    dto.setLimiteCredito(
        br.com.api_caderneta.util.Regras.dinheiro(
            dto.getLimiteCredito() == null
                ? lojas
                    .findById(br.com.api_caderneta.lojas.LojaAtual.exigida())
                    .orElseThrow()
                    .getLimiteCreditoPadrao()
                : dto.getLimiteCredito(),
            true));
    dto.setPrazoPagamentoPadraoDias(
        br.com.api_caderneta.util.Regras.prazo(
            dto.getPrazoPagamentoPadraoDias() == null ? 30 : dto.getPrazoPagamentoPadraoDias()));
    if (repository.existsByCpf(dto.getCpf())) {
      logger.warn("Tentativa de criar cliente com CPF já cadastrado.");
      throw new BusinessException("Já existe um cliente cadastrado com o CPF informado.");
    }
    var entity = mapper.parseObject(dto, Cliente.class);
    entity.setFiador(fiadores.salvar(dto.getFiador(), dto.getCpf()));
    var savedEntity = repository.saveAndFlush(entity);
    logger.info("Cliente criado com sucesso. ID: {}", savedEntity.getId());

    notificacaoService.enviarNotificacao(
        savedEntity,
        "Novo cliente cadastrado: " + savedEntity.getNome(),
        TipoNotificacao.CADASTRO_CLIENTE);
    auditoria.registrar(
        "CLIENTE_CRIADO",
        "CLIENTE",
        savedEntity.getId(),
        br.com.api_caderneta.audit.AuditoriaService.mudanca("nome", null, savedEntity.getNome()));

    return mapper.parseObject(savedEntity, ClienteDTO.class);
  }

  @Transactional
  public ClienteDTO updateCliente(Long id, ClienteUpdateRequestDTO dto) {
    if (dto.temLimite() || dto.temPrazo()) gerente();
    var entity =
        repository
            .bloquear(id)
            .orElseThrow(
                () -> {
                  logger.error("Falha na atualização. Cliente não encontrado com o ID: {}", id);
                  return new ResourceNotFoundException("Cliente não encontrado com o ID: " + id);
                });

    entity.exigirAtivo();
    var antes =
        java.util.Arrays.asList(
            entity.getNome(),
            entity.getEmail(),
            entity.getTelefone(),
            entity.getEndereco(),
            entity.getLimiteCredito(),
            entity.getPrazoPagamentoPadraoDias(),
            entity.getFiador() == null ? null : entity.getFiador().getId());
    entity.setNome(
        dto.getNome() != null
            ? br.com.api_caderneta.util.Regras.nome(dto.getNome())
            : entity.getNome());
    entity.setEmail(dto.temEmail() ? dto.getEmail() : entity.getEmail());
    entity.setTelefone(dto.temTelefone() ? dto.getTelefone() : entity.getTelefone());
    entity.setEndereco(dto.temEndereco() ? dto.getEndereco() : entity.getEndereco());
    entity.setLimiteCredito(
        dto.temLimite()
            ? br.com.api_caderneta.util.Regras.dinheiro(dto.getLimiteCredito(), true)
            : entity.getLimiteCredito());
    entity.setPrazoPagamentoPadraoDias(
        dto.temPrazo()
            ? br.com.api_caderneta.util.Regras.prazo(dto.getPrazoPagamentoPadraoDias())
            : entity.getPrazoPagamentoPadraoDias());

    if (dto.temFiador()) entity.setFiador(fiadores.salvar(dto.getFiador(), entity.getCpf()));
    var updatedEntity = repository.save(entity);
    var depois =
        java.util.Arrays.asList(
            entity.getNome(),
            entity.getEmail(),
            entity.getTelefone(),
            entity.getEndereco(),
            entity.getLimiteCredito(),
            entity.getPrazoPagamentoPadraoDias(),
            entity.getFiador() == null ? null : entity.getFiador().getId());
    auditoria.registrar(
        "CLIENTE_EDITADO",
        "CLIENTE",
        id,
        br.com.api_caderneta.audit.AuditoriaService.mudanca("cadastro", antes, depois));
    logger.info("Cliente com ID: {} atualizado com sucesso", id);
    return mapper.parseObject(updatedEntity, ClienteDTO.class);
  }

  @Transactional
  public void deleteCliente(Long id) {
    alterarAtivo(id, false);
  }

  @Transactional
  public ClienteDTO alterarAtivo(Long id, boolean ativo) {
    var perfil = br.com.api_caderneta.security.UsuarioAtual.obrigatorio().getPerfil();
    if (perfil == br.com.api_caderneta.model.enums.Perfil.CLIENTE)
      throw new org.springframework.security.access.AccessDeniedException(
          "Operação exclusiva da equipe.");
    if (!ativo && perfil != br.com.api_caderneta.model.enums.Perfil.GERENTE)
      throw new org.springframework.security.access.AccessDeniedException(
          "Somente gerente desativa clientes.");
    var cliente =
        repository
            .bloquear(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
    if (cliente.isAtivo() != ativo) {
      cliente.setAtivo(ativo);
      auditoria.registrar(
          ativo ? "CLIENTE_REATIVADO" : "CLIENTE_DESATIVADO",
          "CLIENTE",
          id,
          br.com.api_caderneta.audit.AuditoriaService.mudanca("ativo", !ativo, ativo));
    }
    return mapper.parseObject(repository.save(cliente), ClienteDTO.class);
  }

  @Transactional
  public ClienteDTO updateLimiteCredito(Long id, LimiteCreditoRequestDTO dto) {
    logger.info("Atualizando limite de crédito para o cliente ID: {}", id);
    var entity =
        repository
            .bloquear(id)
            .orElseThrow(
                () -> {
                  logger.error(
                      "Falha ao atualizar limite. Cliente não encontrado com o ID: {}", id);
                  return new ResourceNotFoundException("Cliente não encontrado com o ID: " + id);
                });
    gerente();
    entity.exigirAtivo();
    var antes = entity.getLimiteCredito();
    entity.setLimiteCredito(
        br.com.api_caderneta.util.Regras.dinheiro(dto.getNovoLimiteCredito(), true));
    auditoria.registrar(
        "CREDITO_ALTERADO",
        "CLIENTE",
        id,
        br.com.api_caderneta.audit.AuditoriaService.mudanca(
            "limiteCredito", antes, entity.getLimiteCredito()));
    var updatedEntity = repository.save(entity);
    logger.info(
        "Limite de crédito do cliente ID: {} atualizado para: {}", id, dto.getNovoLimiteCredito());
    return mapper.parseObject(updatedEntity, ClienteDTO.class);
  }

  @Transactional
  public ClienteDTO updatePrazoPagamento(Long id, PrazoPagamentoRequestDTO dto) {
    logger.info("Atualizando prazo de pagamento para o cliente ID: {}", id);
    var entity =
        repository
            .bloquear(id)
            .orElseThrow(
                () -> {
                  logger.error("Falha ao atualizar prazo. Cliente não encontrado com o ID: {}", id);
                  return new ResourceNotFoundException("Cliente não encontrado com o ID: " + id);
                });
    gerente();
    entity.exigirAtivo();
    var antes = entity.getPrazoPagamentoPadraoDias();
    entity.setPrazoPagamentoPadraoDias(
        br.com.api_caderneta.util.Regras.prazo(dto.getNovoPrazoPagamentoDias()));
    auditoria.registrar(
        "PRAZO_ALTERADO",
        "CLIENTE",
        id,
        br.com.api_caderneta.audit.AuditoriaService.mudanca(
            "prazoPagamentoPadraoDias", antes, entity.getPrazoPagamentoPadraoDias()));
    var updatedEntity = repository.save(entity);
    logger.info(
        "Prazo de pagamento do cliente ID: {} atualizado para: {} dias",
        id,
        dto.getNovoPrazoPagamentoDias());
    return mapper.parseObject(updatedEntity, ClienteDTO.class);
  }
}
