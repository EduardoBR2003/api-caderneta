package br.com.api_caderneta.mocks;

import br.com.api_caderneta.dto.FuncionarioDTO;
import br.com.api_caderneta.dto.FuncionarioRequestDTO;
import br.com.api_caderneta.model.Funcionario;

public class MockFuncionario {

    public Funcionario mockEntity() {
        return mockEntity(0L);
    }

    public FuncionarioDTO mockDTO() {
        return mockDTO(0L);
    }

    public FuncionarioRequestDTO mockRequestDTO() {
        FuncionarioRequestDTO dto = new FuncionarioRequestDTO();
        dto.setNome("Func Teste0");
        dto.setCpf("987.654.321-00");
        dto.setEmail("func0@email.com");
        dto.setTelefone("62988888880");
        dto.setEndereco("Av Teste 0");
        dto.setMatricula("FNC000");
        dto.setCargo("Vendedor");
        dto.setLogin("func.teste0");
        dto.setSenhaHash("senha123");
        return dto;
    }

    public Funcionario mockEntity(Long number) {
        Funcionario func = new Funcionario();
        func.setId(number);
        func.setNome("Func Teste" + number);
        func.setCpf("987.654.321-0" + number);
        func.setEmail("func" + number + "@email.com");
        func.setTelefone("6298888888" + number);
        func.setEndereco("Av Teste " + number);
        func.setMatricula("FNC00" + number);
        func.setCargo("Vendedor");
        func.setLogin("func.teste" + number);
        func.setSenhaHash("senha123");
        return func;
    }

    public FuncionarioDTO mockDTO(Long number) {
        FuncionarioDTO dto = new FuncionarioDTO();
        dto.setId(number);
        dto.setNome("Func Teste" + number);
        dto.setCpf("987.654.321-0" + number);
        dto.setEmail("func" + number + "@email.com");
        dto.setTelefone("6298888888" + number);
        dto.setEndereco("Av Teste " + number);
        dto.setMatricula("FNC00" + number);
        dto.setCargo("Vendedor");
        dto.setLogin("func.teste" + number);
        return dto;
    }
}