package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.DividaDTO;
import br.com.api_caderneta.dto.PagamentoRequestDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.Pagamento;
import br.com.api_caderneta.repository.DividaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DividaService {

    private final DividaRepository dividaRepository;
    private final DataMapper mapper;

    @Autowired
    public DividaService(DividaRepository dividaRepository, DataMapper mapper) {
        this.dividaRepository = dividaRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public DividaDTO getDividaById(Long id) {
        var divida = dividaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dívida não encontrada com o ID: " + id));
        return mapper.parseObject(divida, DividaDTO.class);
    }

    @Transactional(readOnly = true)
    public List<DividaDTO> getDividasByCliente(Long clienteId) {
        var dividas = dividaRepository.findByClienteId(clienteId);
        return mapper.parseListObjects(dividas, DividaDTO.class);
    }

    @Transactional
    public DividaDTO registrarPagamento(Long dividaId, PagamentoRequestDTO dto) {
        var divida = dividaRepository.findById(dividaId)
                .orElseThrow(() -> new ResourceNotFoundException("Dívida não encontrada com o ID: " + dividaId));

        if (dto.getValorPago().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor do pagamento deve ser maior que zero.");
        }
        if (dto.getValorPago().compareTo(divida.getValorPendente()) > 0) {
            throw new BusinessException("O valor do pagamento não pode ser maior que o valor pendente.");
        }

        var pagamento = mapper.parseObject(dto, Pagamento.class);
        divida.adicionarPagamento(pagamento);

        var updatedDivida = dividaRepository.save(divida);
        return mapper.parseObject(updatedDivida, DividaDTO.class);
    }
}