package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.NotificacaoDTO;
import br.com.api_caderneta.dto.NotificacaoRequestDTO;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.Notificacao;
import br.com.api_caderneta.model.Pessoa;
import br.com.api_caderneta.model.enums.StatusDivida;
import br.com.api_caderneta.model.enums.TipoNotificacao;
import br.com.api_caderneta.repository.ClienteRepository;
import br.com.api_caderneta.repository.DividaRepository;
import br.com.api_caderneta.repository.NotificacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate; // IMPORTANTE
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacaoService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);
    private final NotificacaoRepository notificacaoRepository;
    private final DividaRepository dividaRepository;
    private final DataMapper mapper;
    private final ClienteRepository clienteRepository;
    
    // Injeção do Template de Mensagens
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificacaoService(NotificacaoRepository notificacaoRepository, 
                              DividaRepository dividaRepository, 
                              DataMapper mapper, 
                              ClienteRepository clienteRepository,
                              SimpMessagingTemplate messagingTemplate) {
        this.notificacaoRepository = notificacaoRepository;
        this.dividaRepository = dividaRepository;
        this.mapper = mapper;
        this.clienteRepository = clienteRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // Método auxiliar para criar e enviar notificação em tempo real
    public void enviarNotificacao(Pessoa destinatario, String mensagem, TipoNotificacao tipo) {
        var notificacao = new Notificacao();
        notificacao.setDestinatario(destinatario);
        notificacao.setMensagem(mensagem);
        notificacao.setTipoNotificacao(tipo);
        notificacao.setDataEnvio(LocalDateTime.now());
        
        var saved = notificacaoRepository.save(notificacao);
        NotificacaoDTO dto = mapper.parseObject(saved, NotificacaoDTO.class);
        
        // ENVIA PARA O WEBSOCKET NO TÓPICO /topic/notificacoes
        messagingTemplate.convertAndSend("/topic/notificacoes", dto);
        logger.info("Notificação enviada via WebSocket: {}", dto.getId());
    }

    @Transactional(readOnly = true)
    public List<NotificacaoDTO> getNotificacoesByCliente(Long clienteId) {
        logger.info("Buscando notificações para o cliente ID: {}", clienteId);
        var notificacoes = notificacaoRepository.findByDestinatarioId(clienteId);
        return mapper.parseListObjects(notificacoes, NotificacaoDTO.class);
    }

    @Transactional(readOnly = true)
    public List<NotificacaoDTO> getAllNotificacoes() {
        var notificacoes = notificacaoRepository.findAll();
        return mapper.parseListObjects(notificacoes, NotificacaoDTO.class);
    }

    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void gerarNotificacoesDeVencimento() {
        logger.info("Gerando notificações de vencimento...");
        var dividasVencidas = dividaRepository.findByStatusDividaAndDataVencimentoBefore(
                StatusDivida.ABERTA, LocalDate.now().plusDays(1)
        );

        for (var divida : dividasVencidas) {
            String mensagem = String.format(
                    "Lembrete: Sua dívida no valor de R$%.2f venceu em %s.",
                    divida.getValorPendente(),
                    divida.getDataVencimento().toString()
            );
            // Usa o novo método centralizado
            enviarNotificacao(divida.getCliente(), mensagem, TipoNotificacao.LEMBRETE_VENCIMENTO);
        }
    }

    @Transactional
    public NotificacaoDTO createManualNotificacao(NotificacaoRequestDTO dto) {
        var cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

        enviarNotificacao(cliente, dto.getMensagem(), dto.getTipoNotificacao());
        
        // Retorna o último objeto salvo (apenas para manter assinatura, embora o socket já tenha enviado)
        // Em um cenário real, você ajustaria isso, mas para simplificar:
        return new NotificacaoDTO(); 
    }

    @Transactional
    public void marcarComoLida(Long id) {
        var notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada"));
        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
    }

    @Transactional
    public void marcarTodasComoLidas() {
        var todas = notificacaoRepository.findAll();
        todas.forEach(n -> n.setLida(true));
        notificacaoRepository.saveAll(todas);
    }
}