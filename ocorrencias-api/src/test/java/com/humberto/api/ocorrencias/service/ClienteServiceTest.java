package com.humberto.api.ocorrencias.service;

import com.humberto.api.ocorrencias.dto.request.ClienteRequest;
import com.humberto.api.ocorrencias.dto.response.ClienteResponse;
import com.humberto.api.ocorrencias.exception.BusinessException;
import com.humberto.api.ocorrencias.exception.ResourceNotFoundException;
import com.humberto.api.ocorrencias.model.Cliente;
import com.humberto.api.ocorrencias.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteService — testes unitários")
class ClienteServiceTest {

    @Mock ClienteRepository clienteRepository;
    @InjectMocks ClienteService clienteService;

    private Cliente clienteSalvo;
    private ClienteRequest request;

    @BeforeEach
    void setUp() {
        clienteSalvo = new Cliente();
        clienteSalvo.setCodCliente(1L);
        clienteSalvo.setNmeCliente("João Silva");
        clienteSalvo.setNroCpf("12345678909");
        clienteSalvo.setDtaNascimento(LocalDate.of(1990, 5, 15));

        request = new ClienteRequest();
        request.setNmeCliente("João Silva");
        request.setNroCpf("123.456.789-09");
        request.setDtaNascimento(LocalDate.of(1990, 5, 15));
    }

    @Test
    @DisplayName("criar: deve salvar cliente com CPF sem máscara")
    void deveCriarCliente() {
        when(clienteRepository.existsByNroCpf("12345678909")).thenReturn(false);
        when(clienteRepository.save(any())).thenReturn(clienteSalvo);

        ClienteResponse response = clienteService.criar(request);

        assertThat(response.getNmeCliente()).isEqualTo("João Silva");
        assertThat(response.getCodCliente()).isEqualTo(1L);
        verify(clienteRepository).save(argThat(c -> c.getNroCpf().equals("12345678909")));
    }

    @Test
    @DisplayName("criar: deve lançar BusinessException para CPF já cadastrado")
    void deveLancarExcecaoParaCpfDuplicado() {
        when(clienteRepository.existsByNroCpf("12345678909")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("buscarPorId: deve retornar cliente existente")
    void deveBuscarPorId() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteSalvo));

        ClienteResponse response = clienteService.buscarPorId(1L);

        assertThat(response.getNmeCliente()).isEqualTo("João Silva");
        assertThat(response.getNroCpf()).isEqualTo("12345678909");
    }

    @Test
    @DisplayName("buscarPorId: deve lançar ResourceNotFoundException para ID inexistente")
    void deveLancarExcecaoParaClienteInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("listar: deve retornar página de clientes")
    void deveListarClientesPaginado() {
        var pageable = PageRequest.of(0, 20);
        when(clienteRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(clienteSalvo)));

        var page = clienteService.listar(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getNmeCliente()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("deletar: deve remover cliente existente")
    void deveDeletarCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteSalvo));

        clienteService.deletar(1L);

        verify(clienteRepository).deleteById(1L);
    }

    @Test
    @DisplayName("buscarOuCriarPorCpf: deve retornar cliente existente sem criar novo")
    void deveBuscarClienteExistentePorCpf() {
        when(clienteRepository.findByNroCpf("12345678909")).thenReturn(Optional.of(clienteSalvo));

        Cliente resultado = clienteService.buscarOuCriarPorCpf(request);

        assertThat(resultado.getCodCliente()).isEqualTo(1L);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("buscarOuCriarPorCpf: deve criar novo cliente quando CPF não existe")
    void deveCriarClienteQuandoCpfNaoExiste() {
        when(clienteRepository.findByNroCpf("12345678909")).thenReturn(Optional.empty());
        when(clienteRepository.save(any())).thenReturn(clienteSalvo);

        Cliente resultado = clienteService.buscarOuCriarPorCpf(request);

        assertThat(resultado).isNotNull();
        verify(clienteRepository).save(any());
    }
}
