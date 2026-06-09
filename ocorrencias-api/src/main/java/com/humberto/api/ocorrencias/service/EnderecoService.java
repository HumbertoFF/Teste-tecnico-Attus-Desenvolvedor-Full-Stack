package com.humberto.api.ocorrencias.service;

import com.humberto.api.ocorrencias.dto.request.EnderecoRequest;
import com.humberto.api.ocorrencias.dto.response.EnderecoResponse;
import com.humberto.api.ocorrencias.exception.ResourceNotFoundException;
import com.humberto.api.ocorrencias.model.Endereco;
import com.humberto.api.ocorrencias.repository.EnderecoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnderecoService {

  private final EnderecoRepository enderecoRepository;

  @Transactional(readOnly = true)
  public Page<EnderecoResponse> listar(Pageable pageable) {
    return enderecoRepository.findAll(pageable).map(EnderecoResponse::from);
  }

  @Transactional(readOnly = true)
  public EnderecoResponse buscarPorId(Long id) {
    return EnderecoResponse.from(buscarEntidade(id));
  }

  @Transactional
  public EnderecoResponse criar(EnderecoRequest request) {
    return EnderecoResponse.from(enderecoRepository.save(fromRequest(request)));
  }

  @Transactional
  public EnderecoResponse atualizar(Long id, EnderecoRequest request) {
    Endereco endereco = buscarEntidade(id);
    endereco.setNmeLogradouro(request.getNmeLogradouro());
    endereco.setNmeBairro(request.getNmeBairro());
    endereco.setNroCep(request.getNroCep().replaceAll("[^0-9]", ""));
    endereco.setNmeCidade(request.getNmeCidade());
    endereco.setNmeEstado(request.getNmeEstado().toUpperCase());
    return EnderecoResponse.from(enderecoRepository.save(endereco));
  }

  @Transactional
  public void deletar(Long id) {
    buscarEntidade(id);
    enderecoRepository.deleteById(id);
  }

  public Endereco buscarEntidade(Long id) {
    return enderecoRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado: " + id));
  }

  public Endereco criarEntidade(EnderecoRequest request) {
    return enderecoRepository.save(fromRequest(request));
  }

  private Endereco fromRequest(EnderecoRequest r) {
    Endereco endereco = new Endereco();
    endereco.setNmeLogradouro(r.getNmeLogradouro());
    endereco.setNmeBairro(r.getNmeBairro());
    endereco.setNroCep(r.getNroCep().replaceAll("[^0-9]", ""));
    endereco.setNmeCidade(r.getNmeCidade());
    endereco.setNmeEstado(r.getNmeEstado().toUpperCase());

    return endereco;
  }
}
