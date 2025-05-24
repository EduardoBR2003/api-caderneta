package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.ClienteDTO;
import br.com.api_caderneta.dto.ClienteRequestDTO;
import br.com.api_caderneta.dto.ClienteUpdateRequestDTO;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper; // Supondo que esta classe exista e funcione
import br.com.api_caderneta.model.Cliente;
import br.com.api_caderneta.model.Fiador;
import br.com.api_caderneta.repository.ClienteRepository;
import br.com.api_caderneta.repository.FiadorRepository; // Necessário para buscar o fiador
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para operações de escrita

import java.util.List;

@Service
public class ClienteService {

    private Logger logger = LoggerFactory.getLogger(ClienteService.class.getName());

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private FiadorRepository fiadorRepository; // Injetar FiadorRepository

    @Transactional(readOnly = true)
    public List<ClienteDTO> findAll(){
        logger.info("Finding all clientes");
        var clientes = clienteRepository.findAll();
        return DataMapper.parseListObjects(clientes, ClienteDTO.class);
    }

    @Transactional(readOnly = true)
    public ClienteDTO findById(Long id){
        logger.info("Finding one cliente with ID: {}", id);
        var cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID: " + id));
        return DataMapper.parseObject(cliente, ClienteDTO.class);
    }

    @Transactional
    public ClienteDTO create (ClienteRequestDTO clienteRequestDTO){
        logger.info("Creating new cliente");

        // Mapear DTO para Entidade
        Cliente clienteEntity = DataMapper.parseObject(clienteRequestDTO, Cliente.class);

        // Lógica para associar Fiador, se fornecido
        if (clienteRequestDTO.getFiadorId() != null) {
            Fiador fiador = fiadorRepository.findById(clienteRequestDTO.getFiadorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fiador not found with ID: " + clienteRequestDTO.getFiadorId()));
            clienteEntity.setFiador(fiador); // [cite: 1]
        } else {
            clienteEntity.setFiador(null);
        }

        Cliente clienteSalvo = clienteRepository.save(clienteEntity);
        logger.info("Created cliente with ID: {}", clienteSalvo.getIdPessoa());
        return DataMapper.parseObject(clienteSalvo, ClienteDTO.class);
    }

    @Transactional
    public ClienteDTO update(Long id, ClienteUpdateRequestDTO clienteUpdateRequestDTO){
        logger.info("Updating cliente with ID: {}", id);

        Cliente entity = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID: " + id));

        // Atualizar campos da entidade a partir do DTO de atualização
        // O DataMapper poderia ajudar aqui, ou fazemos manualmente:
        entity.setNome(clienteUpdateRequestDTO.getNome());
        entity.setEndereco(clienteUpdateRequestDTO.getEndereco());
        entity.setEmail(clienteUpdateRequestDTO.getEmail());
        entity.setTelefone(clienteUpdateRequestDTO.getTelefone());
        entity.setLimiteCredito(clienteUpdateRequestDTO.getLimiteCredito()); // [cite: 1]
        entity.setPrazoPagamentoPadraoDias(clienteUpdateRequestDTO.getPrazoPagamentoPadraoDias()); // [cite: 1]

        // Lógica para atualizar Fiador, se fornecido
        if (clienteUpdateRequestDTO.getFiadorId() != null) {
            if (entity.getFiador() == null || !entity.getFiador().getIdPessoa().equals(clienteUpdateRequestDTO.getFiadorId())) {
                Fiador fiador = fiadorRepository.findById(clienteUpdateRequestDTO.getFiadorId())
                        .orElseThrow(() -> new ResourceNotFoundException("Fiador not found with ID: " + clienteUpdateRequestDTO.getFiadorId()));
                entity.setFiador(fiador); // [cite: 1]
            }
        } else {
            entity.setFiador(null); // Permite desassociar o fiador
        }

        Cliente clienteAtualizado = clienteRepository.save(entity);
        logger.info("Updated cliente with ID: {}", clienteAtualizado.getIdPessoa());
        return DataMapper.parseObject(clienteAtualizado, ClienteDTO.class);
    }

    @Transactional
    public void delete(Long id){
        logger.info("Deleting cliente with ID: {}", id);
        var clienteEntity = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID: " + id));
        clienteRepository.delete(clienteEntity);
        logger.info("Deleted cliente with ID: {}", id);
    }
}