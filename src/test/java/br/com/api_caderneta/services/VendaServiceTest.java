package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.ItemVendaRequestDTO;
import br.com.api_caderneta.dto.VendaDTO;
import br.com.api_caderneta.dto.VendaRequestDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.mocks.MockCliente;
import br.com.api_caderneta.mocks.MockFuncionario;
import br.com.api_caderneta.mocks.MockVenda;
import br.com.api_caderneta.model.Cliente;
import br.com.api_caderneta.model.Funcionario;
import br.com.api_caderneta.model.ItemVenda;
import br.com.api_caderneta.model.Venda;
import br.com.api_caderneta.repository.ClienteRepository;
import br.com.api_caderneta.repository.FuncionarioRepository;
import br.com.api_caderneta.repository.VendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    MockVenda input;
    MockCliente mockCliente;
    MockFuncionario mockFuncionario;

    @InjectMocks
    private VendaService service;

    @Mock
    private VendaRepository vendaRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private DataMapper mapper;

    @BeforeEach
    void setUp() {
        input = new MockVenda();
        mockCliente = new MockCliente();
        mockFuncionario = new MockFuncionario();
    }

    private void mockItemVendaMapping() {
        // Mock inteligente que cria um ItemVenda real a partir do DTO recebido
        when(mapper.parseObject(any(ItemVendaRequestDTO.class), eq(ItemVenda.class)))
                .thenAnswer(invocation -> {
                    ItemVendaRequestDTO dto = invocation.getArgument(0);
                    ItemVenda item = new ItemVenda();
                    item.setDescricaoProduto(dto.getDescricaoProduto());
                    item.setQuantidade(dto.getQuantidade());
                    item.setPrecoUnitario(dto.getPrecoUnitario());
                    return item;
                });
    }

    @Test
    void testCreateVenda_Success() {
        // Arrange
        Cliente cliente = mockCliente.mockEntity(1L);
        cliente.setLimiteCredito(new BigDecimal("200.00")); // Limite maior que o valor da venda
        Funcionario funcionario = mockFuncionario.mockEntity(1L);
        VendaRequestDTO requestDTO = input.mockRequestDTO(1L, 1L);
        
        // Criar venda com dívida válida
        Venda vendaSalva = input.mockEntity(cliente, funcionario);
        br.com.api_caderneta.model.Divida divida = new br.com.api_caderneta.model.Divida();
        divida.setId(1L);
        vendaSalva.setDividaGerada(divida);
        
        VendaDTO expectedDto = new VendaDTO();
        expectedDto.setDividaId(1L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(funcionario));
        mockItemVendaMapping();
        when(vendaRepository.save(any(Venda.class))).thenReturn(vendaSalva);
        when(mapper.parseObject(any(Venda.class), eq(VendaDTO.class))).thenReturn(expectedDto);

        // Act
        VendaDTO result = service.createVenda(requestDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getDividaId());
        verify(vendaRepository, times(1)).save(any(Venda.class));
    }

    @Test
    void testCreateVenda_ExceedsCreditLimit() {
        // Arrange
        Cliente cliente = mockCliente.mockEntity(1L);
        cliente.setLimiteCredito(new BigDecimal("100.00")); // Limite < valor da venda (150)
        Funcionario funcionario = mockFuncionario.mockEntity(1L);
        VendaRequestDTO requestDTO = input.mockRequestDTO(1L, 1L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(funcionario));
        mockItemVendaMapping(); // Usa o mock inteligente para garantir o cálculo correto do valor

        // Act & Assert
        Exception exception = assertThrows(BusinessException.class, () -> {
            service.createVenda(requestDTO);
        });

        assertEquals("O valor da venda excede o limite de crédito do cliente.", exception.getMessage());
        verify(vendaRepository, never()).save(any(Venda.class));
    }

    @Test
    void testCreateVenda_ClienteNotFound() {
        // Arrange
        VendaRequestDTO requestDTO = input.mockRequestDTO(1L, 1L);
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            service.createVenda(requestDTO);
        });

        assertEquals("Cliente não encontrado com o ID: 1", exception.getMessage());
    }

    @Test
    void testCreateVenda_FuncionarioNotFound() {
        // Arrange
        Cliente cliente = mockCliente.mockEntity(1L);
        VendaRequestDTO requestDTO = input.mockRequestDTO(1L, 1L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            service.createVenda(requestDTO);
        });

        assertEquals("Funcionário não encontrado com o ID: 1", exception.getMessage());
    }
}