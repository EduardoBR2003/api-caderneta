package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.FiadorDTO;
import br.com.api_caderneta.dto.FiadorRequestDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.Fiador;
import br.com.api_caderneta.repository.ClienteRepository;
import br.com.api_caderneta.repository.FiadorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FiadorService {

    private static final Logger logger = LoggerFactory.getLogger(FiadorService.class);

    private final FiadorRepository fiadorRepository;
    private final ClienteRepository clienteRepository;
    private final DataMapper mapper;

    @Autowired
    public FiadorService(FiadorRepository fiadorRepository, ClienteRepository clienteRepository, DataMapper mapper) {
        this.fiadorRepository = fiadorRepository;
        this.clienteRepository = clienteRepository;
        this.mapper = mapper;
    }

    @Transactional
    public FiadorDTO createFiador(FiadorRequestDTO dto) {
        logger.info("Criando novo fiador com CPF: {}", dto.getCpf());
        if (fiadorRepository.existsByCpf(dto.getCpf())) {
            logger.warn("Tentativa de criar fiador com CPF duplicado: {}", dto.getCpf());
            throw new BusinessException("Já existe um fiador cadastrado com o CPF informado.");
        }
        var entity = mapper.parseObject(dto, Fiador.class);
        var savedEntity = fiadorRepository.save(entity);
        logger.info("Fiador criado com sucesso. ID: {}", savedEntity.getId());
        return mapper.parseObject(savedEntity, FiadorDTO.class);
    }

    @Transactional(readOnly = true)
    public FiadorDTO getFiadorById(Long id) {
        logger.info("Buscando fiador com ID: {}", id);
        var entity = fiadorRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Fiador não encontrado com o ID: {}", id);
                    return new ResourceNotFoundException("Fiador não encontrado com o ID: " + id);
                });
        return mapper.parseObject(entity, FiadorDTO.class);
    }

    @Transactional(readOnly = true)
    public List<FiadorDTO> getAllFiadores() {
        logger.info("Buscando todos os fiadores");
        var entities = fiadorRepository.findAll();
        return mapper.parseListObjects(entities, FiadorDTO.class);
    }

    @Transactional
    public void associarFiador(Long clienteId, Long fiadorId) {
        logger.info("Associando fiador ID {} ao cliente ID {}", fiadorId, clienteId);
        var cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + clienteId));

        var fiador = fiadorRepository.findById(fiadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Fiador não encontrado com o ID: " + fiadorId));

        cliente.setFiador(fiador);
        clienteRepository.save(cliente);
        logger.info("Associação realizada com sucesso.");
    }
}