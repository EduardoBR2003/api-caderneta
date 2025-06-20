package br.com.api_caderneta.mocks;

import br.com.api_caderneta.dto.FuncionarioDTO;
import br.com.api_caderneta.dto.FuncionarioRequestDTO;
import br.com.api_caderneta.dto.FuncionarioUpdateRequestDTO;
import br.com.api_caderneta.model.Proprietario;

import java.util.ArrayList;
import java.util.List;

public class MockProprietario {

    public Proprietario mockEntity() {
        return mockEntity(0);
    }

    public FuncionarioDTO mockDTO() {
        return mockDTO(0);
    }

    public FuncionarioRequestDTO mockRequestDTO() {
        return mockRequestDTO(0);
    }

    public List<Proprietario> mockEntityList() {
        List<Proprietario> proprietarios = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            proprietarios.add(mockEntity(i));
        }
        return proprietarios;
    }

    public Proprietario mockEntity(Integer number) {
        var proprietario = new Proprietario();
        proprietario.setId(number.longValue());
        proprietario.setNome("Nome Proprietario " + number);
        proprietario.setCpf("1234567890" + number);
        proprietario.setEmail("prop" + number + "@teste.com");
        proprietario.setTelefone("99999999" + number);
        proprietario.setEndereco("Endereco " + number);
        proprietario.setLogin("login" + number);
        proprietario.setSenhaHash("senha" + number);
        proprietario.setCargo("Proprietário");
        return proprietario;
    }

    public FuncionarioDTO mockDTO(Integer number) {
        var dto = new FuncionarioDTO();
        dto.setId(number.longValue());
        dto.setNome("Nome Proprietario " + number);
        dto.setCpf("1234567890" + number);
        dto.setEmail("prop" + number + "@teste.com");
        dto.setTelefone("99999999" + number);
        dto.setEndereco("Endereco " + number);
        dto.setLogin("login" + number);
        dto.setCargo("Proprietário");
        return dto;
    }

    public FuncionarioRequestDTO mockRequestDTO(Integer number) {
        var dto = new FuncionarioRequestDTO();
        dto.setNome("Nome Proprietario " + number);
        dto.setCpf("1234567890" + number);
        dto.setEmail("prop" + number + "@teste.com");
        dto.setTelefone("99999999" + number);
        dto.setEndereco("Endereco " + number);
        dto.setLogin("login" + number);
        dto.setSenhaHash("senha" + number);
        dto.setCargo("Proprietário");
        return dto;
    }
}