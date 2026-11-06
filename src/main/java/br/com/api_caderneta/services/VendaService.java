package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.VendaDTO;
import br.com.api_caderneta.dto.VendaRequestDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.*;
import br.com.api_caderneta.model.enums.StatusDivida;
import br.com.api_caderneta.model.enums.TipoNotificacao; // Import necessário
import br.com.api_caderneta.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import br.com.api_caderneta.util.Fuso;
import br.com.api_caderneta.audit.AuditoriaService;
import br.com.api_caderneta.exceptions.ConflictException;
import br.com.api_caderneta.security.UsuarioAtual;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class VendaService {

    private static final Logger logger = LoggerFactory.getLogger(VendaService.class);
    private static final java.util.List<br.com.api_caderneta.model.enums.StatusDivida> STATUS_EM_ABERTO = java.util.List.of(
            br.com.api_caderneta.model.enums.StatusDivida.ABERTA,
            br.com.api_caderneta.model.enums.StatusDivida.PAGA_PARCIALMENTE,
            br.com.api_caderneta.model.enums.StatusDivida.VENCIDA);
    private final VendaRepository vendaRepository;
    private final ClienteRepository clienteRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final DataMapper mapper;
    
    private final NotificacaoService notificacaoService;
    private final AuditoriaService auditoria;
    private final br.com.api_caderneta.repository.DividaRepository dividaRepository;

    @Autowired
    public VendaService(VendaRepository vendaRepository, 
                        ClienteRepository clienteRepository, 
                        FuncionarioRepository funcionarioRepository, 
                        DataMapper mapper,
                        NotificacaoService notificacaoService,
                        AuditoriaService auditoria,
                        br.com.api_caderneta.repository.DividaRepository dividaRepository) {
        this.dividaRepository = dividaRepository;
        this.vendaRepository = vendaRepository;
        this.clienteRepository = clienteRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.mapper = mapper;
        this.notificacaoService = notificacaoService;
        this.auditoria = auditoria;
    }

    @Transactional
    public VendaDTO createVenda(VendaRequestDTO dto) {
        logger.info("Iniciando processo de criação de venda para o cliente ID: {}", dto.getClienteId());
        
        // Validações para criação
        if (dto.getClienteId() == null) {
            throw new BusinessException("Cliente é obrigatório para criar uma venda");
        }
        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new BusinessException("Itens são obrigatórios para criar uma venda");
        }
        // Defesa no serviço: não depende de a validação do controller estar ativa.
        for (var item : dto.getItens()) {
            if (item == null || item.getQuantidade() == null || item.getQuantidade() < 1) {
                throw new BusinessException("A quantidade de cada item deve ser de pelo menos 1.");
            }
            if (item.getPrecoUnitario() == null || item.getPrecoUnitario().signum() <= 0) {
                throw new BusinessException("O preço unitário de cada item deve ser maior que zero.");
            }
        }
        
        var cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> {
                    logger.error("Cliente não encontrado com o ID: {}", dto.getClienteId());
                    return new ResourceNotFoundException("Cliente não encontrado com o ID: " + dto.getClienteId());
                });

        // O responsável é o usuário da sessão; o funcionarioId do corpo (legado) é ignorado.
        var responsavelId = UsuarioAtual.obrigatorio().getPessoaId();
        var funcionario = funcionarioRepository.findById(responsavelId)
                .orElseThrow(() -> new BusinessException("O usuário autenticado não é um funcionário."));

        var novaVenda = new Venda();
        novaVenda.setCliente(cliente);
        novaVenda.setFuncionario(funcionario);

        dto.getItens().forEach(itemDto -> {
            var itemVenda = mapper.parseObject(itemDto, ItemVenda.class);
            novaVenda.adicionarItemVenda(itemVenda);
        });
        logger.debug("Itens adicionados à venda. Valor total calculado: {}", novaVenda.getValorTotal());

        BigDecimal valorTotalVenda = novaVenda.getValorTotal();
        if (cliente.getLimiteCredito() != null) {
            // O limite vale para o total em aberto: saldo devedor atual + esta venda.
            BigDecimal saldo = dividaRepository.somaPendenteDoCliente(cliente.getId(), STATUS_EM_ABERTO);
            BigDecimal exposicao = (saldo == null ? BigDecimal.ZERO : saldo).add(valorTotalVenda);
            if (exposicao.compareTo(cliente.getLimiteCredito()) > 0) {
                logger.warn("Venda para o cliente ID {} bloqueada. Saldo + venda (R${}) excede o limite (R${}).", cliente.getId(), exposicao, cliente.getLimiteCredito());
                throw new BusinessException("O valor da venda excede o limite de crédito do cliente.");
            }
        }

        var divida = criarDividaParaVenda(novaVenda, cliente);
        novaVenda.setDividaGerada(divida);
        logger.info("Dívida gerada para a nova venda. Valor: R${}", divida.getValorOriginal());

        var savedVenda = vendaRepository.save(novaVenda);
        logger.info("Venda criada com sucesso. ID: {}. Dívida associada ID: {}", savedVenda.getId(), savedVenda.getDividaGerada().getId());

        try {
            String mensagemNotificacao = String.format("Nova compra realizada no valor de R$ %s (Venda #%d)", 
                    savedVenda.getValorTotal(), savedVenda.getId());
            
            // Registra o evento de histórico do cliente
            notificacaoService.enviarNotificacao(cliente, mensagemNotificacao, TipoNotificacao.COMPRA_REALIZADA);
            
        } catch (Exception e) {
            // Logamos o erro mas não impedimos a venda de ser concluída se a notificação falhar
            logger.error("Erro ao registrar o evento de histórico da venda ID: {}", savedVenda.getId(), e);
        }
        auditoria.registrar("VENDA_CRIADA", "VENDA", savedVenda.getId(), AuditoriaService.mudanca(
                "clienteId", null, cliente.getId(), "valorTotal", null, savedVenda.getValorTotal(),
                "funcionarioId", null, funcionario.getId()));

        return mapper.parseObject(savedVenda, VendaDTO.class);
    }

    private Divida criarDividaParaVenda(Venda venda, Cliente cliente) {
        var divida = new Divida();
        divida.setVendaOrigem(venda);
        divida.setCliente(cliente);
        divida.setValorOriginal(venda.getValorTotal());
        divida.setValorPendente(venda.getValorTotal());
        divida.setStatusDivida(StatusDivida.ABERTA);

        Integer prazo = cliente.getPrazoPagamentoPadraoDias() != null ? cliente.getPrazoPagamentoPadraoDias() : 30; // Prazo padrão de 30 dias
        divida.setDataVencimento(Fuso.hoje().plusDays(prazo));

        return divida;
    }

    @Transactional(readOnly = true)
    public java.util.List<VendaDTO> getAllVendas() {
        logger.info("Buscando todas as vendas");
        var vendas = vendaRepository.findAll();
        logger.info("Encontradas {} vendas no sistema", vendas.size());
        return mapper.parseListObjects(vendas, VendaDTO.class);
    }

    @Transactional(readOnly = true)
    public VendaDTO getVendaById(Long id) {
        logger.info("Buscando venda com ID: {}", id);
        var venda = vendaRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Venda não encontrada com o ID: {}", id);
                    return new ResourceNotFoundException("Venda não encontrada com o ID: " + id);
                });
        return mapper.parseObject(venda, VendaDTO.class);
    }

    @Transactional
    public VendaDTO updateVenda(Long id, VendaRequestDTO dto) {
        logger.info("Atualizando venda com ID: {}", id);
        var venda = vendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada com o ID: " + id));
        
        var antesCliente = venda.getCliente().getId();
        var antesValor = venda.getValorTotal();
        var antesData = venda.getDataHora();
        var divida = venda.getDividaGerada();
        boolean temPagamentos = divida != null && divida.getPagamentos() != null && !divida.getPagamentos().isEmpty();

        // O funcionário responsável pela venda não é reatribuído por este endpoint (funcionarioId ignorado).
        if (dto.getClienteId() != null && !dto.getClienteId().equals(antesCliente)) {
            if (temPagamentos) {
                throw new ConflictException("Não é possível trocar o cliente de uma venda cuja dívida já tem pagamentos.");
            }
            var cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + dto.getClienteId()));
            venda.setCliente(cliente);
            if (divida != null) divida.setCliente(cliente);
        }
        if (dto.getDataHora() != null) {
            venda.setDataHora(dto.getDataHora());
        }
        if (dto.getValorTotal() != null && dto.getValorTotal().compareTo(antesValor) != 0) {
            if (dto.getValorTotal().signum() <= 0) {
                throw new BusinessException("O valor total deve ser maior que zero.");
            }
            if (temPagamentos) {
                throw new ConflictException("Não é possível alterar o valor de uma venda cuja dívida já tem pagamentos.");
            }
            venda.setValorTotal(dto.getValorTotal());
            if (divida != null) { // mantém venda, dívida e saldo consistentes
                divida.setValorOriginal(dto.getValorTotal());
                divida.setValorPendente(dto.getValorTotal());
            }
        }

        var vendaAtualizada = vendaRepository.save(venda);
        auditoria.registrar("VENDA_EDITADA", "VENDA", id, AuditoriaService.mudanca(
                "clienteId", antesCliente, vendaAtualizada.getCliente().getId(),
                "valorTotal", antesValor, vendaAtualizada.getValorTotal(),
                "dataHora", antesData, vendaAtualizada.getDataHora()));
        logger.info("Venda com ID: {} atualizada com sucesso", id);
        return mapper.parseObject(vendaAtualizada, VendaDTO.class);
    }

    @Transactional
    public void deleteVenda(Long id) {
        logger.info("Excluindo venda com ID: {}", id);
        var venda = vendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada com o ID: " + id));
        
        var divida = venda.getDividaGerada();
        if (divida != null && divida.getPagamentos() != null && !divida.getPagamentos().isEmpty()) {
            throw new ConflictException("Não é possível excluir uma venda cuja dívida já tem pagamentos registrados.");
        }
        var valor = venda.getValorTotal();
        var clienteId = venda.getCliente().getId();
        vendaRepository.delete(venda);
        auditoria.registrar("VENDA_EXCLUIDA", "VENDA", id, AuditoriaService.mudanca(
                "clienteId", clienteId, null, "valorTotal", valor, null));
        logger.info("Venda com ID: {} excluída com sucesso", id);
    }
}