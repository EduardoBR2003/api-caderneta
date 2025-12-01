package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.DividaDTO;
import br.com.api_caderneta.dto.PagamentoRequestDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.Pagamento;
import br.com.api_caderneta.model.enums.TipoNotificacao; // Import necessário
import br.com.api_caderneta.repository.DividaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DividaService {

    private static final Logger logger = LoggerFactory.getLogger(DividaService.class);
    private final DividaRepository dividaRepository;
    private final DataMapper mapper;
    
    // Serviço para notificação em tempo real
    private final NotificacaoService notificacaoService;

    @Autowired
    public DividaService(DividaRepository dividaRepository, DataMapper mapper, NotificacaoService notificacaoService) {
        this.dividaRepository = dividaRepository;
        this.mapper = mapper;
        this.notificacaoService = notificacaoService;
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
        var divida = dividaRepository.findById(id)
                .orElseThrow(() -> {
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

    @Transactional
    public DividaDTO registrarPagamento(Long dividaId, PagamentoRequestDTO dto) {
        logger.info("Registrando pagamento de {} para a dívida ID: {}", dto.getValorPago(), dividaId);
        var divida = dividaRepository.findById(dividaId)
                .orElseThrow(() -> {
                    logger.error("Falha ao registrar pagamento. Dívida não encontrada com ID: {}", dividaId);
                    return new ResourceNotFoundException("Dívida não encontrada com o ID: " + dividaId);
                });

        if (dto.getValorPago().compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Tentativa de registrar pagamento com valor inválido: {}", dto.getValorPago());
            throw new BusinessException("O valor do pagamento deve ser maior que zero.");
        }
        if (dto.getValorPago().compareTo(divida.getValorPendente()) > 0) {
            logger.warn("Tentativa de pagamento (R${}) maior que o valor pendente (R${}) para a dívida ID: {}", dto.getValorPago(), divida.getValorPendente(), dividaId);
            throw new BusinessException("O valor do pagamento não pode ser maior que o valor pendente.");
        }

        var pagamento = mapper.parseObject(dto, Pagamento.class);
        divida.adicionarPagamento(pagamento);

        var updatedDivida = dividaRepository.save(divida);
        logger.info("Pagamento registrado com sucesso para a dívida ID: {}. Novo status: {}", dividaId, updatedDivida.getStatusDivida());

        // --- INÍCIO DA INTEGRAÇÃO COM WEBSOCKET ---
        try {
            String mensagem = String.format("Pagamento de R$ %s recebido para a dívida ID: %d", 
                    dto.getValorPago(), dividaId);
            
            // Envia notificação ao cliente confirmando o pagamento
            notificacaoService.enviarNotificacao(updatedDivida.getCliente(), mensagem, TipoNotificacao.PAGAMENTO_RECEBIDO);
        } catch (Exception e) {
            logger.error("Erro ao enviar notificação de pagamento", e);
        }
        // --- FIM ---

        return mapper.parseObject(updatedDivida, DividaDTO.class);
    }

    @Transactional
    public DividaDTO updateDivida(Long id, DividaDTO dto) {
        logger.info("Atualizando dívida com ID: {}", id);
        var divida = dividaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dívida não encontrada com o ID: " + id));
        
        var dividaAtualizada = dividaRepository.save(divida);
        return mapper.parseObject(dividaAtualizada, DividaDTO.class);
    }

    @Transactional
    public void deleteDivida(Long id) {
        logger.info("Excluindo dívida com ID: {}", id);
        var divida = dividaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dívida não encontrada com o ID: " + id));
        
        dividaRepository.delete(divida);
        logger.info("Dívida com ID: {} excluída com sucesso", id);
    }
}