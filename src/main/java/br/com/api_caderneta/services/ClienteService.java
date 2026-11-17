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

  // Serviço para notificação em tempo real
  private final NotificacaoService notificacaoService;
  @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbc;

  private record Saldo(java.math.BigDecimal valor, boolean atraso) {}
  private java.util.Map<Long,Saldo> saldos(List<Cliente> clientes) {
    if (clientes.isEmpty()) return java.util.Map.of();
    String marcadores = String.join(",", java.util.Collections.nCopies(clientes.size(), "?"));
    var args = new java.util.ArrayList<Object>();
    args.add(br.com.api_caderneta.util.Fuso.hoje());
    args.add(br.com.api_caderneta.lojas.LojaAtual.exigida());
    clientes.forEach(c -> args.add(c.getId()));
    return jdbc.query("SELECT cliente_id,COALESCE(SUM(valor_pendente),0),MAX(CASE WHEN valor_pendente>0 AND data_vencimento<? THEN 1 ELSE 0 END) FROM dividas WHERE status_divida<>'CANCELADA' AND loja_id=? AND cliente_id IN ("+marcadores+") GROUP BY cliente_id",
      (org.springframework.jdbc.core.ResultSetExtractor<java.util.Map<Long,Saldo>>) rs -> {
        var out = new java.util.HashMap<Long,Saldo>();
        while (rs.next()) out.put(rs.getLong(1), new Saldo(rs.getBigDecimal(2),rs.getInt(3)==1));
        return out;
      }, args.toArray());
  }
  private ClienteResponseDTO resposta(Cliente c, java.util.Map<Long,Saldo> saldos) {
    var dto = mapper.parseObject(c, ClienteResponseDTO.class);
    var saldo = saldos.getOrDefault(c.getId(),new Saldo(java.math.BigDecimal.ZERO,false));
    dto.setSaldoDevedor(saldo.valor()); dto.setEmAtraso(saldo.atraso());
    return dto;
  }

  @Autowired private br.com.api_caderneta.audit.AuditoriaService auditoria;
  @Autowired private br.com.api_caderneta.repository.FiadorRepository fiadores;
  @Autowired private br.com.api_caderneta.lojas.LojaRepository lojas;

  private void gerente() {
    if (br.com.api_caderneta.security.UsuarioAtual.obrigatorio().getPerfil()
        != br.com.api_caderneta.model.enums.Perfil.GERENTE)
      throw new org.springframework.security.access.AccessDeniedException(
          "Somente gerente pode alterar limite e prazo.");
  }

  /** Cliente não pode ser fiador de si mesmo (o CPF é único por papel, então a regra é explícita). */
  static void naoEhOProprioFiador(String cpfCliente, String cpfFiador) {
    var a = cpfCliente == null ? "" : cpfCliente.replaceAll("\\D", "");
    var b = cpfFiador == null ? "" : cpfFiador.replaceAll("\\D", "");
    if (!a.isEmpty() && a.equals(b)) {
      throw new BusinessException("O cliente não pode ser fiador de si mesmo.");
    }
  }

  private br.com.api_caderneta.model.Fiador fiador(FiadorRequestDTO dto, String cpfCliente) {
    if (dto == null) return null;
    var f =
        dto.getId() == null
            ? fiadores
                .porCpf(br.com.api_caderneta.util.Regras.cpf(dto.getCpf()))
                .orElseGet(br.com.api_caderneta.model.Fiador::new)
            : fiadores
                .findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Fiador não encontrado."));
    if (f.getId() != null) repository.bloquearPorFiador(f.getId()).forEach(Cliente::exigirAtivo);
    naoEhOProprioFiador(cpfCliente, dto.getId() == null ? dto.getCpf() : f.getCpf());
    var antes =
        java.util.Arrays.asList(f.getNome(), f.getEmail(), f.getTelefone(), f.getEndereco());
    f.setNome(br.com.api_caderneta.util.Regras.nome(dto.getNome()));
    f.setCpf(br.com.api_caderneta.util.Regras.cpf(dto.getCpf()));
    f.setTelefone(dto.getTelefone());
    f.setEmail(dto.getEmail());
    f.setEndereco(dto.getEndereco());
    f = fiadores.saveAndFlush(f);
    auditoria.registrar(
        "FIADOR_SALVO",
        "FIADOR",
        f.getId(),
        br.com.api_caderneta.audit.AuditoriaService.mudanca(
            "cadastro",
            antes,
            java.util.Arrays.asList(f.getNome(), f.getEmail(), f.getTelefone(), f.getEndereco())));
    return f;
  }

  @Transactional(readOnly = true)
  public br.com.api_caderneta.auth.Dtos.Pagina<ClienteResponseDTO> listar(
      int page, int size, String q) {
    org.springframework.data.jpa.domain.Specification<Cliente> spec =
        (root, query, cb) -> {
          String texto = q == null ? "" : q.strip().toLowerCase(java.util.Locale.ROOT);
          var nomes = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
          for (String parte : texto.split("\\s+")) nomes.add(cb.like(cb.lower(root.get("nome")), "%" + parte.replace("%", "\\%").replace("_", "\\_") + "%", '\\'));
          String digitos = texto.replaceAll("\\D", "");
          if (digitos.isEmpty()) return cb.and(nomes.toArray(jakarta.persistence.criteria.Predicate[]::new));
          jakarta.persistence.criteria.Expression<String> telefone = root.get("telefone");
          jakarta.persistence.criteria.Expression<String> cpf = root.get("cpf");
          for (String simbolo : java.util.List.of(".", "-", " ", "(", ")", "+")) {
            telefone = cb.function("replace", String.class, telefone, cb.literal(simbolo), cb.literal(""));
            cpf = cb.function("replace", String.class, cpf, cb.literal(simbolo), cb.literal(""));
          }
          return cb.or(cb.and(nomes.toArray(jakarta.persistence.criteria.Predicate[]::new)), cb.like(telefone, "%"+digitos+"%"), cb.like(cpf, "%"+digitos+"%"));
        };
    var pagina = repository.findAll(spec,
      org.springframework.data.domain.PageRequest.of(Math.max(0,page),Math.min(100,Math.max(1,size)),
        org.springframework.data.domain.Sort.by("nome","id")));
    var totais = saldos(pagina.getContent());
    return br.com.api_caderneta.auth.Dtos.Pagina.de(pagina.map(c -> resposta(c, totais)));
  }

  @Autowired
  public ClienteService(
      ClienteRepository repository, DataMapper mapper, NotificacaoService notificacaoService) {
    this.repository = repository;
    this.mapper = mapper;
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
              ? lojas.findById(br.com.api_caderneta.lojas.LojaAtual.exigida()).orElseThrow().getLimiteCreditoPadrao()
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
    entity.setFiador(fiador(dto.getFiador(), dto.getCpf()));
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

    if (dto.temFiador()) entity.setFiador(fiador(dto.getFiador(), entity.getCpf()));
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
      throw new org.springframework.security.access.AccessDeniedException("Operação exclusiva da equipe.");
    if (!ativo && perfil != br.com.api_caderneta.model.enums.Perfil.GERENTE)
      throw new org.springframework.security.access.AccessDeniedException("Somente gerente desativa clientes.");
    var cliente = repository.bloquear(id).orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
    if (cliente.isAtivo() != ativo) {
      cliente.setAtivo(ativo);
      auditoria.registrar(ativo ? "CLIENTE_REATIVADO" : "CLIENTE_DESATIVADO", "CLIENTE", id,
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
