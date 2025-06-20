package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.NotificacaoDTO;
import br.com.api_caderneta.dto.NotificacaoRequestDTO;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.Notificacao;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class NotificacaoService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);
    private final NotificacaoRepository notificacaoRepository;
    private final DividaRepository dividaRepository;
    private final DataMapper mapper;
    private final ClienteRepository clienteRepository;

    @Autowired
    public NotificacaoService(NotificacaoRepository notificacaoRepository, DividaRepository dividaRepository, DataMapper mapper, ClienteRepository clienteRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.dividaRepository = dividaRepository;
        this.mapper = mapper;
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificacaoDTO> getNotificacoesByCliente(Long clienteId) {
        logger.info("Buscando notificações para o cliente ID: {}", clienteId);
        var notificacoes = notificacaoRepository.findByDestinatarioId(clienteId);
        return mapper.parseListObjects(notificacoes, NotificacaoDTO.class);
    }

    /**
     * Simula o envio de notificações para dívidas vencidas.
     * Roda todo dia à 01:00 da manhã.
     * A expressão cron é: (segundo minuto hora dia-do-mês mês dia-da-semana)
     */
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void gerarNotificacoesDeVencimento() {
        logger.info("Iniciando tarefa agendada: Geração de notificações de vencimento.");
        var dividasVencidas = dividaRepository.findByStatusDividaAndDataVencimentoBefore(
                StatusDivida.ABERTA, LocalDate.now().plusDays(1)
        );

        logger.info("Encontradas {} dívidas vencidas ou vencendo hoje.", dividasVencidas.size());

        for (var divida : dividasVencidas) {
            String mensagem = String.format(
                    "Lembrete: Sua dívida no valor de R$%.2f, referente à venda de ID %d, venceu em %s.",
                    divida.getValorPendente(),
                    divida.getVendaOrigem().getId(),
                    divida.getDataVencimento().toString()
            );

            var notificacao = new Notificacao();
            notificacao.setDestinatario(divida.getCliente());
            notificacao.setMensagem(mensagem);
            notificacao.setTipoNotificacao(TipoNotificacao.LEMBRETE_VENCIMENTO);

            notificacaoRepository.save(notificacao);
            logger.debug("Notificação de vencimento gerada para a dívida ID: {}", divida.getId());
        }
        logger.info("Tarefa agendada finalizada.");
    }

    @Transactional
    public NotificacaoDTO createManualNotificacao(NotificacaoRequestDTO dto) {
        logger.info("Criando notificação manual para o cliente ID: {}", dto.getClienteId());

        var cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + dto.getClienteId()));

        var notificacao = new Notificacao();
        notificacao.setDestinatario(cliente);
        notificacao.setMensagem(dto.getMensagem());
        notificacao.setTipoNotificacao(dto.getTipoNotificacao());
        notificacao.setDataEnvio(LocalDate.now().atStartOfDay());

        var savedNotificacao = notificacaoRepository.save(notificacao);
        logger.info("Notificação manual criada com sucesso. ID: {}", savedNotificacao.getIdNotificacao());

        return mapper.parseObject(savedNotificacao, NotificacaoDTO.class);
    }
}