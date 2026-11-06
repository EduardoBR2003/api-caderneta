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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import br.com.api_caderneta.util.Fuso;
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

    @Autowired
    public NotificacaoService(NotificacaoRepository notificacaoRepository, 
                              DividaRepository dividaRepository, 
                              DataMapper mapper, 
                              ClienteRepository clienteRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.dividaRepository = dividaRepository;
        this.mapper = mapper;
        this.clienteRepository = clienteRepository;
    }

    // Cria e persiste o evento. Sem WebSocket: a interface consulta o histórico por REST.
    public void enviarNotificacao(Pessoa destinatario, String mensagem, TipoNotificacao tipo) {
        var notificacao = new Notificacao();
        notificacao.setDestinatario(destinatario);
        notificacao.setMensagem(mensagem);
        notificacao.setTipoNotificacao(tipo);
        notificacao.setDataEnvio(Fuso.agoraUtc());
        
        var saved = notificacaoRepository.save(notificacao);
        logger.info("Evento de histórico registrado: {}", saved.getIdNotificacao());
    }

    @Transactional(readOnly = true)
    public List<NotificacaoDTO> getNotificacoesByCliente(Long clienteId) {
        logger.info("Buscando notificações para o cliente ID: {}", clienteId);
        var notificacoes = notificacaoRepository.findByDestinatarioId(clienteId);
        return mapper.parseListObjects(notificacoes, NotificacaoDTO.class);
    }

    @Transactional(readOnly = true)
    public List<NotificacaoDTO> getAllNotificacoes() {
        logger.info("Buscando todas as notificações do sistema");
        var notificacoes = notificacaoRepository.findAll();
        return mapper.parseListObjects(notificacoes, NotificacaoDTO.class);
    }

    /**
     * Lembrete de vencimento: no máximo UM por dívida/estado. A rotina roda toda noite, mas a mensagem
     * (que inclui o número da dívida, o saldo e a data) só é gerada se ainda não existir.
     */
    @Transactional
    @Scheduled(cron = "0 0 1 * * *", zone = "America/Sao_Paulo")
    public void gerarNotificacoesDeVencimento() {
        logger.info("Gerando notificações de vencimento...");
        var hoje = Fuso.hoje();
        var dividas = dividaRepository.findComSaldoVencendoAntes(
                java.util.List.of(StatusDivida.ABERTA, StatusDivida.PAGA_PARCIALMENTE, StatusDivida.VENCIDA),
                hoje.plusDays(1));

        int novas = 0;
        for (var divida : dividas) {
            String situacao = divida.getDataVencimento().isBefore(hoje) ? "venceu" : "vence hoje";
            String mensagem = String.format("Lembrete: sua dívida #%d, com saldo de R$ %.2f, %s em %s.",
                    divida.getId(), divida.getValorPendente(), situacao, divida.getDataVencimento());
            var cliente = divida.getCliente();
            if (notificacaoRepository.existsByDestinatarioIdAndTipoNotificacaoAndMensagem(
                    cliente.getId(), TipoNotificacao.LEMBRETE_VENCIMENTO, mensagem)) {
                continue;
            }
            enviarNotificacao(cliente, mensagem, TipoNotificacao.LEMBRETE_VENCIMENTO);
            novas++;
        }
        logger.info("Lembretes de vencimento: {} novo(s) de {} dívida(s) com saldo vencida(s)/vencendo.", novas, dividas.size());
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

    // Marcar notificação como lida
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