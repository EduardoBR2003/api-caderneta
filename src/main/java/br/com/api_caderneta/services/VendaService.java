package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.VendaDTO;
import br.com.api_caderneta.dto.VendaRequestDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.*;
import br.com.api_caderneta.model.enums.StatusDivida;
import br.com.api_caderneta.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class VendaService {

    private static final Logger logger = LoggerFactory.getLogger(VendaService.class);
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
        logger.info("Iniciando processo de criação de venda para o cliente ID: {}", dto.getClienteId());
        
        // Validações para criação
        if (dto.getClienteId() == null) {
            throw new BusinessException("Cliente é obrigatório para criar uma venda");
        }
        if (dto.getFuncionarioId() == null) {
            throw new BusinessException("Funcionário é obrigatório para criar uma venda");
        }
        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new BusinessException("Itens são obrigatórios para criar uma venda");
        }
        
        var cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> {
                    logger.error("Cliente não encontrado com o ID: {}", dto.getClienteId());
                    return new ResourceNotFoundException("Cliente não encontrado com o ID: " + dto.getClienteId());
                });

        var funcionario = funcionarioRepository.findById(dto.getFuncionarioId())
                .orElseThrow(() -> {
                    logger.error("Funcionário não encontrado com o ID: {}", dto.getFuncionarioId());
                    return new ResourceNotFoundException("Funcionário não encontrado com o ID: " + dto.getFuncionarioId());
                });

        var novaVenda = new Venda();
        novaVenda.setCliente(cliente);
        novaVenda.setFuncionario(funcionario);

        dto.getItens().forEach(itemDto -> {
            var itemVenda = mapper.parseObject(itemDto, ItemVenda.class);
            novaVenda.adicionarItemVenda(itemVenda);
        });
        logger.debug("Itens adicionados à venda. Valor total calculado: {}", novaVenda.getValorTotal());


        BigDecimal valorTotalVenda = novaVenda.getValorTotal();
        if (cliente.getLimiteCredito() != null && valorTotalVenda.compareTo(cliente.getLimiteCredito()) > 0) {
            logger.warn("Venda para o cliente ID {} bloqueada. Valor (R${}) excede o limite de crédito (R${}).", cliente.getId(), valorTotalVenda, cliente.getLimiteCredito());
            throw new BusinessException("O valor da venda excede o limite de crédito do cliente.");
        }

        var divida = criarDividaParaVenda(novaVenda, cliente);
        novaVenda.setDividaGerada(divida);
        logger.info("Dívida gerada para a nova venda. Valor: R${}", divida.getValorOriginal());


        var savedVenda = vendaRepository.save(novaVenda);
        logger.info("Venda criada com sucesso. ID: {}. Dívida associada ID: {}", savedVenda.getId(), savedVenda.getDividaGerada().getId());
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
        
        // Atualizar cliente se fornecido
        if (dto.getClienteId() != null) {
            var cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + dto.getClienteId()));
            venda.setCliente(cliente);
            logger.info("Cliente da venda atualizado para ID: {}", dto.getClienteId());
        }
        
        // Atualizar funcionário se fornecido
        if (dto.getFuncionarioId() != null) {
            var funcionario = funcionarioRepository.findById(dto.getFuncionarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado com o ID: " + dto.getFuncionarioId()));
            venda.setFuncionario(funcionario);
            logger.info("Funcionário da venda atualizado para ID: {}", dto.getFuncionarioId());
        }
        
        // Atualizar data se fornecida
        if (dto.getDataHora() != null) {
            venda.setDataHora(dto.getDataHora());
            logger.info("Data da venda atualizada para: {}", dto.getDataHora());
        }
        
        // Atualizar valor total se fornecido (para casos especiais)
        if (dto.getValorTotal() != null) {
            // Nota: Normalmente o valor total é calculado pelos itens,
            // mas permitimos atualização manual para casos especiais
            logger.info("Valor total da venda será atualizado manualmente de {} para {}", 
                       venda.getValorTotal(), dto.getValorTotal());
            venda.setValorTotal(dto.getValorTotal());
        }
        
        var vendaAtualizada = vendaRepository.save(venda);
        logger.info("Venda com ID: {} atualizada com sucesso", id);
        return mapper.parseObject(vendaAtualizada, VendaDTO.class);
    }

    @Transactional
    public void deleteVenda(Long id) {
        logger.info("Excluindo venda com ID: {}", id);
        var venda = vendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada com o ID: " + id));
        
        vendaRepository.delete(venda);
        logger.info("Venda com ID: {} excluída com sucesso", id);
    }
}