package br.com.api_caderneta.services;

import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.mocks.MockCliente;
import br.com.api_caderneta.mocks.MockFuncionario;
import br.com.api_caderneta.mocks.MockVenda;
import br.com.api_caderneta.model.Cliente;
import br.com.api_caderneta.model.Funcionario;
import br.com.api_caderneta.model.Venda;
import br.com.api_caderneta.repository.DividaRepository;
import br.com.api_caderneta.repository.VendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @InjectMocks
    private RelatorioService service;

    @Mock
    private VendaRepository vendaRepository;
    @Mock
    private DividaRepository dividaRepository;
    @Mock
    private DataMapper mapper;

    @BeforeEach
    void setUp() {
        lenient().when(mapper.parseListObjects(any(), any()))
                .thenAnswer(invocation -> {
                    List<?> sourceList = invocation.getArgument(0);
                    Class<?> destClass = invocation.getArgument(1);
                    return sourceList.stream()
                            .map(source -> new ModelMapper().map(source, destClass))
                            .toList();
                });
    }

    @Test
    void testGerarRelatorioVendas() {
        LocalDate dataInicio = LocalDate.of(2024, 1, 1);
        LocalDate dataFim = LocalDate.of(2024, 1, 31);

        Cliente cliente = new MockCliente().mockEntity(1L);
        Funcionario funcionario = new MockFuncionario().mockEntity(1L);
        Venda venda = new MockVenda().mockEntity(cliente, funcionario); // Valor total 150.00
        List<Venda> vendas = Collections.singletonList(venda);

        // Os dias do relatório são os de Brasília (UTC-3): 01/01 00:00 BRT = 01/01 03:00 UTC, até 31/01 23:59:59.999999999 BRT.
        var inicioUtc = java.time.LocalDateTime.of(2024, 1, 1, 3, 0);
        var fimUtc = java.time.LocalDateTime.of(2024, 2, 1, 3, 0).minusNanos(1);
        when(vendaRepository.findAllByDataHoraBetween(eq(inicioUtc), eq(fimUtc))).thenReturn(vendas);

        var relatorio = service.gerarRelatorioVendas(dataInicio, dataFim);

        assertNotNull(relatorio);
        assertEquals(1, relatorio.getTotalVendas());
        assertEquals(new BigDecimal("150.00"), relatorio.getFaturamentoTotal());
        assertEquals(1, relatorio.getVendas().size());
    }
}