package br.com.api_caderneta.mocks;

import br.com.api_caderneta.dto.ClienteDTO;
import br.com.api_caderneta.dto.ClienteRequestDTO;
import br.com.api_caderneta.model.Cliente;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MockCliente {

    public Cliente mockEntity() {
        return mockEntity(0L);
    }

    public ClienteDTO mockDTO() {
        return mockDTO(0L);
    }

    public ClienteRequestDTO mockRequestDTO() {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNome("Nome Teste0");
        dto.setCpf("123.456.789-00");
        dto.setEmail("teste0@email.com");
        dto.setTelefone("62999999990");
        dto.setEndereco("Rua Teste 0");
        dto.setLimiteCredito(new BigDecimal("1000.00"));
        dto.setPrazoPagamentoPadraoDias(30);
        return dto;
    }

    public Cliente mockEntity(Long number) {
        Cliente cliente = new Cliente();
        cliente.setId(number);
        cliente.setNome("Nome Teste" + number);
        cliente.setCpf("123.456.789-0" + number);
        cliente.setEmail("teste" + number + "@email.com");
        cliente.setTelefone("6299999999" + number);
        cliente.setEndereco("Rua Teste " + number);
        cliente.setLimiteCredito(new BigDecimal("1000.00"));
        cliente.setPrazoPagamentoPadraoDias(30);
        return cliente;
    }

    public ClienteDTO mockDTO(Long number) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(number);
        dto.setNome("Nome Teste" + number);
        dto.setCpf("123.456.789-0" + number);
        dto.setEmail("teste" + number + "@email.com");
        dto.setTelefone("6299999999" + number);
        dto.setEndereco("Rua Teste " + number);
        dto.setLimiteCredito(new BigDecimal("1000.00"));
        dto.setPrazoPagamentoPadraoDias(30);
        return dto;
    }

    public List<Cliente> mockEntityList() {
        List<Cliente> clientes = new ArrayList<>();
        for (long i = 0; i < 5; i++) {
            clientes.add(mockEntity(i));
        }
        return clientes;
    }

    public List<ClienteDTO> mockDTOList() {
        List<ClienteDTO> dtos = new ArrayList<>();
        for (long i = 0; i < 5; i++) {
            dtos.add(mockDTO(i));
        }
        return dtos;
    }
}