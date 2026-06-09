package com.humberto.api.ocorrencias.service;

import com.humberto.api.ocorrencias.dto.request.ClienteRequest;
import com.humberto.api.ocorrencias.dto.request.EnderecoRequest;
import com.humberto.api.ocorrencias.dto.request.OcorrenciaRequest;
import com.humberto.api.ocorrencias.dto.response.OcorrenciaResponse;
import com.humberto.api.ocorrencias.exception.BusinessException;
import com.humberto.api.ocorrencias.exception.ResourceNotFoundException;
import com.humberto.api.ocorrencias.model.Cliente;
import com.humberto.api.ocorrencias.model.Endereco;
import com.humberto.api.ocorrencias.model.Ocorrencia;
import com.humberto.api.ocorrencias.model.enums.StatusOcorrencia;
import com.humberto.api.ocorrencias.repository.OcorrenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OcorrenciaService — testes unitários")
class OcorrenciaServiceTest {

    @Mock OcorrenciaRepository ocorrenciaRepository;
    @Mock ClienteService clienteService;
    @Mock EnderecoService enderecoService;
    @Mock LocalStorageService storageService;
    @Mock FotoOcorrenciaService fotoOcorrenciaService;

    @InjectMocks OcorrenciaService ocorrenciaService;

    private Ocorrencia ocorrenciaAtiva;
    private Ocorrencia ocorrenciaFinalizada;
    private Cliente cliente;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setCodCliente(1L);
        cliente.setNmeCliente("Maria Santos");
        cliente.setNroCpf("98765432100");
        cliente.setDtaNascimento(LocalDate.of(1985, 8, 22));

        endereco = new Endereco();
        endereco.setCodEndereco(1L);
        endereco.setNmeLogradouro("Rua XV de Novembro, 200");
        endereco.setNmeBairro("Centro");
        endereco.setNroCep("80020310");
        endereco.setNmeCidade("Curitiba");
        endereco.setNmeEstado("PR");

        ocorrenciaAtiva = new Ocorrencia();
        ocorrenciaAtiva.setCodOcorrencia(1L);
        ocorrenciaAtiva.setCliente(cliente);
        ocorrenciaAtiva.setEndereco(endereco);
        ocorrenciaAtiva.setDtaOcorrencia(LocalDate.now());
        ocorrenciaAtiva.setStaOcorrencia(StatusOcorrencia.ATIVA);
        ocorrenciaAtiva.setFotos(new ArrayList<>());

        ocorrenciaFinalizada = new Ocorrencia();
        ocorrenciaFinalizada.setCodOcorrencia(2L);
        ocorrenciaFinalizada.setCliente(cliente);
        ocorrenciaFinalizada.setEndereco(endereco);
        ocorrenciaFinalizada.setDtaOcorrencia(LocalDate.now());
        ocorrenciaFinalizada.setStaOcorrencia(StatusOcorrencia.FINALIZADA);
        ocorrenciaFinalizada.setFotos(new ArrayList<>());
    }

    @Test
    @DisplayName("cadastrar: deve criar ocorrência com cliente e endereço")
    void deveCadastrarOcorrencia() {
        when(clienteService.buscarOuCriarPorCpf(any())).thenReturn(cliente);
        when(enderecoService.criarEntidade(any())).thenReturn(endereco);
        when(ocorrenciaRepository.save(any())).thenReturn(ocorrenciaAtiva);
        when(ocorrenciaRepository.findById(1L)).thenReturn(Optional.of(ocorrenciaAtiva));

        OcorrenciaResponse response = ocorrenciaService.cadastrar(buildRequest(), List.of());

        assertThat(response).isNotNull();
        assertThat(response.getCodOcorrencia()).isEqualTo(1L);
        assertThat(response.getStaOcorrencia()).isEqualTo(StatusOcorrencia.ATIVA);
        assertThat(response.getCliente().getNmeCliente()).isEqualTo("Maria Santos");
        verify(ocorrenciaRepository).save(any());
    }

    @Test
    @DisplayName("cadastrar: deve reutilizar cliente existente pelo CPF")
    void deveBuscarOuCriarCliente() {
        when(clienteService.buscarOuCriarPorCpf(any())).thenReturn(cliente);
        when(enderecoService.criarEntidade(any())).thenReturn(endereco);
        when(ocorrenciaRepository.save(any())).thenReturn(ocorrenciaAtiva);
        when(ocorrenciaRepository.findById(1L)).thenReturn(Optional.of(ocorrenciaAtiva));

        ocorrenciaService.cadastrar(buildRequest(), null);

        verify(clienteService).buscarOuCriarPorCpf(any());
        verify(enderecoService).criarEntidade(any());
    }

    @Test
    @DisplayName("buscarPorId: deve retornar ocorrência existente")
    void deveBuscarPorId() {
        when(ocorrenciaRepository.findById(1L)).thenReturn(Optional.of(ocorrenciaAtiva));

        OcorrenciaResponse response = ocorrenciaService.buscarPorId(1L);

        assertThat(response.getCodOcorrencia()).isEqualTo(1L);
        assertThat(response.getEndereco().getNmeCidade()).isEqualTo("Curitiba");
    }

    @Test
    @DisplayName("buscarPorId: deve lançar ResourceNotFoundException para ID inexistente")
    void deveLancarExcecaoParaIdInexistente() {
        when(ocorrenciaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ocorrenciaService.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("finalizar: deve alterar status de ATIVA para FINALIZADA")
    void deveFinalizar() {
        when(ocorrenciaRepository.findById(1L)).thenReturn(Optional.of(ocorrenciaAtiva));
        when(ocorrenciaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OcorrenciaResponse response = ocorrenciaService.finalizar(1L);

        assertThat(response.getStaOcorrencia()).isEqualTo(StatusOcorrencia.FINALIZADA);
        verify(ocorrenciaRepository).save(ocorrenciaAtiva);
    }

    @Test
    @DisplayName("finalizar: deve lançar BusinessException se já estiver FINALIZADA")
    void deveLancarExcecaoAoFinalizarJaFinalizada() {
        when(ocorrenciaRepository.findById(2L)).thenReturn(Optional.of(ocorrenciaFinalizada));

        assertThatThrownBy(() -> ocorrenciaService.finalizar(2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("finalizada");

        verify(ocorrenciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("deletar: deve remover ocorrência ATIVA com sucesso")
    void deveDeletarOcorrenciaAtiva() {
        when(ocorrenciaRepository.findById(1L)).thenReturn(Optional.of(ocorrenciaAtiva));

        ocorrenciaService.deletar(1L);

        verify(ocorrenciaRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deletar: deve lançar BusinessException ao tentar deletar ocorrência FINALIZADA")
    void deveLancarExcecaoAoDeletarFinalizada() {
        when(ocorrenciaRepository.findById(2L)).thenReturn(Optional.of(ocorrenciaFinalizada));

        assertThatThrownBy(() -> ocorrenciaService.deletar(2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("finalizada");

        verify(ocorrenciaRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deletar: deve lançar ResourceNotFoundException para ID inexistente")
    void deveLancarExcecaoAoDeletarInexistente() {
        when(ocorrenciaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ocorrenciaService.deletar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private OcorrenciaRequest buildRequest() {
        ClienteRequest clienteReq = new ClienteRequest();
        clienteReq.setNmeCliente("Maria Santos");
        clienteReq.setNroCpf("987.654.321-00");
        clienteReq.setDtaNascimento(LocalDate.of(1985, 8, 22));

        EnderecoRequest enderecoReq = new EnderecoRequest();
        enderecoReq.setNmeLogradouro("Rua XV de Novembro, 200");
        enderecoReq.setNmeBairro("Centro");
        enderecoReq.setNroCep("80020310");
        enderecoReq.setNmeCidade("Curitiba");
        enderecoReq.setNmeEstado("PR");

        OcorrenciaRequest req = new OcorrenciaRequest();
        req.setCliente(clienteReq);
        req.setEndereco(enderecoReq);
        return req;
    }
}
