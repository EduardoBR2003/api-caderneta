package br.com.api_caderneta.services;

import br.com.api_caderneta.audit.AuditoriaService;
import br.com.api_caderneta.dto.DividaDTO;
import br.com.api_caderneta.dto.PagamentoRequestDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ConflictException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.Pagamento;
import br.com.api_caderneta.model.enums.TipoNotificacao; // Import necessário
import br.com.api_caderneta.repository.DividaRepository;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DividaService {

  private final AuditoriaService auditoria;
  @Autowired private br.com.api_caderneta.repository.ClienteRepository clientes;

  private static final Logger logger = LoggerFactory.getLogger(DividaService.class);
  private final DividaRepository dividaRepository;
  private final DataMapper mapper;

  // Serviço para notificação em tempo real
  private final NotificacaoService notificacaoService;

  @Autowired
  public DividaService(
      DividaRepository dividaRepository,
      DataMapper mapper,
      NotificacaoService notificacaoService,
      AuditoriaService auditoria) {
    this.auditoria = auditoria;
    this.dividaRepository = dividaRepository;
    this.mapper = mapper;
    this.notificacaoService = notificacaoService;
  }

  @Transactional(readOnly = true)
  public br.com.api_caderneta.auth.Dtos.Pagina<DividaDTO> listar(
      int page,
      int size,
      String q,
      String status,
      java.time.LocalDate inicio,
      java.time.LocalDate fim) {
    org.springframework.data.jpa.domain.Specification<br.com.api_caderneta.model.Divida> spec =
        (root, query, cb) -> {
          var ps = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
          if (q != null && !q.isBlank())
            ps.add(
                cb.like(
                    cb.lower(root.get("cliente").get("nome")),
                    "%" + q.toLowerCase(java.util.Locale.ROOT) + "%"));
          if (status != null && !status.isBlank())
            ps.add(br.com.api_caderneta.util.Consultas.status(cb, root, status));
          if (inicio != null) ps.add(cb.greaterThanOrEqualTo(root.get("dataEmissao"), inicio));
          if (fim != null) ps.add(cb.lessThanOrEqualTo(root.get("dataEmissao"), fim));
          return cb.and(ps.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    return br.com.api_caderneta.auth.Dtos.Pagina.de(
        dividaRepository
            .findAll(
                spec,
                org.springframework.data.domain.PageRequest.of(
                    Math.max(0, page),
                    Math.min(100, Math.max(1, size)),
                    org.springframework.data.domain.Sort.by("dataVencimento", "id")))
            .map(d -> mapper.parseObject(d, DividaDTO.class)));
  }

  @Transactional(readOnly = true)
  public List<DividaDTO> getAllDividas() {
    logger.info("Buscando todas as dívidas");
    var dividas = dividaRepository.findAll();
    logger.info("Encontradas {} dívidas no sistema", dividas.size());
    return mapper.parseListObjects(dividas, DividaDTO.class);
  }

  @Transactional(readOnly = true)
  public DividaDTO getDividaById(Long id) {
    logger.info("Buscando dívida com ID: {}", id);
    var divida =
        dividaRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  logger.error("Dívida não encontrada com o ID: {}", id);
                  return new ResourceNotFoundException("Dívida não encontrada com o ID: " + id);
                });
    return mapper.parseObject(divida, DividaDTO.class);
  }

  @Transactional(readOnly = true)
  public List<DividaDTO> getDividasByCliente(Long clienteId) {
    logger.info("Buscando dívidas para o cliente de ID: {}", clienteId);
    var dividas = dividaRepository.findByClienteId(clienteId);
    logger.info("Encontradas {} dívidas para o cliente ID: {}", dividas.size(), clienteId);
    return mapper.parseListObjects(dividas, DividaDTO.class);
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public DividaDTO registrarPagamento(Long dividaId, PagamentoRequestDTO dto) {
    logger.info("Registrando pagamento de {} para a dívida ID: {}", dto.getValorPago(), dividaId);
    var cid =
        dividaRepository
            .clienteId(dividaId)
            .orElseThrow(() -> new ResourceNotFoundException("Dívida não encontrada."));
    clientes.bloquear(cid).orElseThrow();
    br.com.api_caderneta.util.Regras.dinheiro(dto.getValorPago(), false);
    if (dto.getMetodoPagamento() == null)
      throw new BusinessException("Informe o método de pagamento.");
    var divida =
        dividaRepository
            .findByIdParaAtualizar(dividaId)
            .orElseThrow(
                () -> {
                  logger.error(
                      "Falha ao registrar pagamento. Dívida não encontrada com ID: {}", dividaId);
                  return new ResourceNotFoundException(
                      "Dívida não encontrada com o ID: " + dividaId);
                });

    if (divida.getStatusDivida() == br.com.api_caderneta.model.enums.StatusDivida.CANCELADA)
      throw new ConflictException("Dívida cancelada.");
    if (dto.getValorPago().compareTo(BigDecimal.ZERO) <= 0) {
      logger.warn("Tentativa de registrar pagamento com valor inválido: {}", dto.getValorPago());
      throw new BusinessException("O valor do pagamento deve ser maior que zero.");
    }
    if (dto.getValorPago().compareTo(divida.getValorPendente()) > 0) {
      logger.warn(
          "Tentativa de pagamento (R${}) maior que o valor pendente (R${}) para a dívida ID: {}",
          dto.getValorPago(),
          divida.getValorPendente(),
          dividaId);
      throw new BusinessException("O valor do pagamento não pode ser maior que o valor pendente.");
    }

    var valorAntes = divida.getValorPendente();
    var pagamento = mapper.parseObject(dto, Pagamento.class);
    pagamento.setOperadorId(br.com.api_caderneta.security.UsuarioAtual.obrigatorio().getId());
    divida.adicionarPagamento(pagamento);

    var updatedDivida = dividaRepository.save(divida);
    logger.info(
        "Pagamento registrado com sucesso para a dívida ID: {}. Novo status: {}",
        dividaId,
        updatedDivida.getStatusDivida());

    notificacaoService.enviarNotificacao(
        updatedDivida.getCliente(),
        "Pagamento de R$ " + dto.getValorPago() + " recebido para a dívida #" + dividaId,
        TipoNotificacao.PAGAMENTO_RECEBIDO);
    auditoria.registrar(
        "PAGAMENTO_REGISTRADO",
        "DIVIDA",
        dividaId,
        AuditoriaService.mudanca(
            "valorPendente",
            valorAntes,
            updatedDivida.getValorPendente(),
            "valorPago",
            null,
            dto.getValorPago()));

    return mapper.parseObject(updatedDivida, DividaDTO.class);
  }

  /**
   * Único campo editável de uma dívida: {@code dataVencimento} (renegociação de prazo). Valores,
   * cliente e pagamentos derivam da venda e dos recebimentos e não são alterados por aqui. Dívida
   * quitada ou cancelada não muda.
   */
  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public DividaDTO updateDivida(Long id, DividaDTO dto) {
    logger.info("Atualizando dívida com ID: {}", id);
    var cid =
        dividaRepository
            .clienteId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dívida não encontrada."));
    clientes.bloquear(cid).orElseThrow();
    var divida =
        dividaRepository
            .findByIdParaAtualizar(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Dívida não encontrada com o ID: " + id));

    if (dto.getDataVencimento() == null) {
      throw new BusinessException(
          "Informe 'dataVencimento': é o único campo editável de uma dívida.");
    }
    var status = divida.getStatusDivida();
    if (status == br.com.api_caderneta.model.enums.StatusDivida.PAGA_TOTALMENTE
        || status == br.com.api_caderneta.model.enums.StatusDivida.CANCELADA) {
      throw new ConflictException("Não é possível alterar uma dívida quitada ou cancelada.");
    }
    if (divida.getDataEmissao() != null
        && dto.getDataVencimento().isBefore(divida.getDataEmissao())) {
      throw new BusinessException("O vencimento não pode ser anterior à data de emissão.");
    }

    var antes = divida.getDataVencimento();
    divida.setDataVencimento(dto.getDataVencimento());
    divida.atualizarStatus(); // vencida deixa de ser vencida (e vice-versa) conforme a nova data
    var dividaAtualizada = dividaRepository.save(divida);
    auditoria.registrar(
        "DIVIDA_EDITADA",
        "DIVIDA",
        id,
        AuditoriaService.mudanca(
            "dataVencimento",
            antes,
            dividaAtualizada.getDataVencimento(),
            "statusDivida",
            status,
            dividaAtualizada.getStatusDivida()));
    return mapper.parseObject(dividaAtualizada, DividaDTO.class);
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public void deleteDivida(Long id) {
    throw new ConflictException(
        "A dívida integra uma venda. Exclua a venda sem pagamentos, preservando a consistência.");
  }
}
