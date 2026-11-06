package br.com.api_caderneta.services;

import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.mocks.MockCliente;
import br.com.api_caderneta.mocks.MockDivida;
import br.com.api_caderneta.mocks.MockFuncionario;
import br.com.api_caderneta.mocks.MockVenda;
import br.com.api_caderneta.model.Divida;
import br.com.api_caderneta.model.Notificacao;
import br.com.api_caderneta.model.enums.TipoNotificacao;
import br.com.api_caderneta.repository.ClienteRepository;
import br.com.api_caderneta.repository.DividaRepository;
import br.com.api_caderneta.repository.NotificacaoRepository;
import br.com.api_caderneta.util.Fuso;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificacaoServiceTest {

    NotificacaoRepository notificacoes = mock(NotificacaoRepository.class);
    DividaRepository dividas = mock(DividaRepository.class);
    NotificacaoService service = new NotificacaoService(notificacoes, dividas, mock(DataMapper.class),
            mock(ClienteRepository.class));

    private Divida vencida() {
        var cliente = new MockCliente().mockEntity(1L);
        var venda = new MockVenda().mockEntity(cliente, new MockFuncionario().mockEntity(1L));
        Divida d = new MockDivida().mockEntity(cliente, venda);
        d.setId(7L);
        d.setDataVencimento(Fuso.hoje().minusDays(3));
        return d;
    }

    @Test
    void lembreteDeUmaDividaNaoSeRepeteNasProximasNoites() {
        var d = vencida();
        when(dividas.findComSaldoVencendoAntes(any(), any())).thenReturn(List.of(d));
        when(notificacoes.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));

        // 1ª noite: ainda não existe -> gera
        when(notificacoes.existsByDestinatarioIdAndTipoNotificacaoAndMensagem(any(), eq(TipoNotificacao.LEMBRETE_VENCIMENTO), any()))
                .thenReturn(false);
        service.gerarNotificacoesDeVencimento();
        var cap = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacoes, times(1)).save(cap.capture());
        String mensagem = cap.getValue().getMensagem();
        assertTrue(mensagem.contains("#7") && mensagem.contains("venceu"));
        assertEquals(TipoNotificacao.LEMBRETE_VENCIMENTO, cap.getValue().getTipoNotificacao());

        // 2ª noite: a mesma mensagem já existe -> não gera de novo
        when(notificacoes.existsByDestinatarioIdAndTipoNotificacaoAndMensagem(any(), eq(TipoNotificacao.LEMBRETE_VENCIMENTO), eq(mensagem)))
                .thenReturn(true);
        service.gerarNotificacoesDeVencimento();
        verify(notificacoes, times(1)).save(any(Notificacao.class));
    }

    @Test
    void vencimentoHojeUsaTextoProprio() {
        var d = vencida();
        d.setDataVencimento(Fuso.hoje());
        when(dividas.findComSaldoVencendoAntes(any(), any())).thenReturn(List.of(d));
        when(notificacoes.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));
        service.gerarNotificacoesDeVencimento();
        var cap = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacoes).save(cap.capture());
        assertTrue(cap.getValue().getMensagem().contains("vence hoje"));
    }
}
