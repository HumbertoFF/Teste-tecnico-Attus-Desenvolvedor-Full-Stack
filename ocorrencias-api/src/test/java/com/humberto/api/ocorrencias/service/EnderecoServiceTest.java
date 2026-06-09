package com.humberto.api.ocorrencias.service;

import com.humberto.api.ocorrencias.dto.request.EnderecoRequest;
import com.humberto.api.ocorrencias.dto.response.EnderecoResponse;
import com.humberto.api.ocorrencias.exception.ResourceNotFoundException;
import com.humberto.api.ocorrencias.model.Endereco;
import com.humberto.api.ocorrencias.repository.EnderecoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnderecoService — testes unitários")
class EnderecoServiceTest {

    @Mock EnderecoRepository enderecoRepository;
    @InjectMocks EnderecoService enderecoService;

    private Endereco enderecoSalvo;
    private EnderecoRequest request;

    @BeforeEach
    void setUp() {
        enderecoSalvo = new Endereco();
        enderecoSalvo.setCodEndereco(1L);
        enderecoSalvo.setNmeLogradouro("Rua XV de Novembro, 200");
        enderecoSalvo.setNmeBairro("Centro");
        enderecoSalvo.setNroCep("80020310");
        enderecoSalvo.setNmeCidade("Curitiba");
        enderecoSalvo.setNmeEstado("PR");

        request = new EnderecoRequest();
        request.setNmeLogradouro("Rua XV de Novembro, 200");
        request.setNmeBairro("Centro");
        request.setNroCep("80020-310");  // com hífen — service deve limpar
        request.setNmeCidade("Curitiba");
        request.setNmeEstado("pr");       // minúsculo — service deve converter para maiúsculo
    }

    @Test
    @DisplayName("criar: deve salvar endereço com CEP limpo e estado em maiúsculo")
    void deveCriarEndereco() {
        when(enderecoRepository.save(any())).thenReturn(enderecoSalvo);

        EnderecoResponse response = enderecoService.criar(request);

        assertThat(response).isNotNull();
        assertThat(response.getNmeCidade()).isEqualTo("Curitiba");
        verify(enderecoRepository).save(argThat(e ->
            e.getNroCep().equals("80020310") &&
            e.getNmeEstado().equals("PR")
        ));
    }

    @Test
    @DisplayName("buscarPorId: deve retornar endereço existente")
    void deveBuscarPorId() {
        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(enderecoSalvo));

        EnderecoResponse response = enderecoService.buscarPorId(1L);

        assertThat(response.getCodEndereco()).isEqualTo(1L);
        assertThat(response.getNmeCidade()).isEqualTo("Curitiba");
    }

    @Test
    @DisplayName("buscarPorId: deve lançar ResourceNotFoundException para ID inexistente")
    void deveLancarExcecaoParaIdInexistente() {
        when(enderecoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enderecoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("listar: deve retornar página de endereços")
    void deveListarEnderecosPaginado() {
        var pageable = PageRequest.of(0, 20);
        when(enderecoRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(enderecoSalvo)));

        var page = enderecoService.listar(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getNmeCidade()).isEqualTo("Curitiba");
    }

    @Test
    @DisplayName("atualizar: deve converter estado para maiúsculo e limpar CEP")
    void deveAtualizarEndereco() {
        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(enderecoSalvo));
        when(enderecoRepository.save(any())).thenReturn(enderecoSalvo);

        EnderecoRequest novoRequest = new EnderecoRequest();
        novoRequest.setNmeLogradouro("Av. Paulista, 1000");
        novoRequest.setNmeBairro("Bela Vista");
        novoRequest.setNroCep("01310-100");
        novoRequest.setNmeCidade("São Paulo");
        novoRequest.setNmeEstado("sp");

        enderecoService.atualizar(1L, novoRequest);

        verify(enderecoRepository).save(argThat(e ->
            e.getNmeEstado().equals("SP") &&
            e.getNroCep().equals("01310100")
        ));
    }

    @Test
    @DisplayName("deletar: deve remover endereço existente")
    void deveDeletarEndereco() {
        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(enderecoSalvo));

        enderecoService.deletar(1L);

        verify(enderecoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deletar: deve lançar ResourceNotFoundException para ID inexistente")
    void deveLancarExcecaoAoDeletarInexistente() {
        when(enderecoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enderecoService.deletar(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(enderecoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("criarEntidade: deve retornar entidade salva com CEP limpo")
    void deveCriarEntidade() {
        when(enderecoRepository.save(any())).thenReturn(enderecoSalvo);

        Endereco resultado = enderecoService.criarEntidade(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodEndereco()).isEqualTo(1L);
        verify(enderecoRepository).save(argThat(e -> e.getNroCep().equals("80020310")));
    }
}
