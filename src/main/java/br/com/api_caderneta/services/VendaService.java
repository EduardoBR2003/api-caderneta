package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.VendaDTO;
import br.com.api_caderneta.dto.VendaRequestDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.*;
import br.com.api_caderneta.model.enums.StatusDivida;
import br.com.api_caderneta.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ClienteRepository clienteRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final DataMapper mapper;

    @Autowired
    public VendaService(VendaRepository vendaRepository, ClienteRepository clienteRepository, FuncionarioRepository funcionarioRepository, DataMapper mapper) {
        this.vendaRepository = vendaRepository;
        this.clienteRepository = clienteRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.mapper = mapper;
    }

    @Transactional
    public VendaDTO createVenda(VendaRequestDTO dto) {
        var cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + dto.getClienteId()));

        var funcionario = funcionarioRepository.findById(dto.getFuncionarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado com o ID: " + dto.getFuncionarioId()));

        var novaVenda = new Venda();
        novaVenda.setCliente(cliente);
        novaVenda.setFuncionario(funcionario);

        dto.getItens().forEach(itemDto -> {
            var itemVenda = mapper.parseObject(itemDto, ItemVenda.class);
            novaVenda.adicionarItemVenda(itemVenda);
        });

        BigDecimal valorTotalVenda = novaVenda.getValorTotal();
        if (cliente.getLimiteCredito() != null && valorTotalVenda.compareTo(cliente.getLimiteCredito()) > 0) {
            throw new BusinessException("O valor da venda excede o limite de crédito do cliente.");
        }

        var divida = criarDividaParaVenda(novaVenda, cliente);
        novaVenda.setDividaGerada(divida);

        var savedVenda = vendaRepository.save(novaVenda);
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
        divida.setDataVencimento(LocalDate.now().plusDays(prazo));

        return divida;
    }

    @Transactional(readOnly = true)
    public VendaDTO getVendaById(Long id) {
        var venda = vendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada com o ID: " + id));
        return mapper.parseObject(venda, VendaDTO.class);
    }
}