package com.humberto.api.ocorrencias.service;

import com.humberto.api.ocorrencias.dto.request.ClienteRequest;
import com.humberto.api.ocorrencias.dto.response.ClienteResponse;
import com.humberto.api.ocorrencias.exception.BusinessException;
import com.humberto.api.ocorrencias.exception.ResourceNotFoundException;
import com.humberto.api.ocorrencias.model.Cliente;
import com.humberto.api.ocorrencias.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService {

  private final ClienteRepository clienteRepository;

  @Transactional(readOnly = true)
  public Page<ClienteResponse> listar(Pageable pageable) {
    return clienteRepository.findAll(pageable).map(ClienteResponse::from);
  }

  @Transactional(readOnly = true)
  public ClienteResponse buscarPorId(Long id) {
    return ClienteResponse.from(buscarEntidade(id));
  }

  @Transactional
  public ClienteResponse criar(ClienteRequest request) {
    String cpfLimpo = limparCpf(request.getNroCpf());
    if (clienteRepository.existsByNroCpf(cpfLimpo)) {
      throw new BusinessException("CPF já cadastrado: " + request.getNroCpf());
    }

    Cliente cliente = new Cliente();
    cliente.setNmeCliente(request.getNmeCliente());
    cliente.setDtaNascimento(request.getDtaNascimento());
    cliente.setNroCpf(cpfLimpo);

    return ClienteResponse.from(clienteRepository.save(cliente));
  }

  @Transactional
  public ClienteResponse atualizar(Long id, ClienteRequest request) {
    Cliente cliente = buscarEntidade(id);
    String cpfLimpo = limparCpf(request.getNroCpf());

    if (!cliente.getNroCpf().equals(cpfLimpo) && clienteRepository.existsByNroCpf(cpfLimpo)) {
      throw new BusinessException("CPF já cadastrado para outro cliente");
    }

    cliente.setNmeCliente(request.getNmeCliente());
    cliente.setDtaNascimento(request.getDtaNascimento());
    cliente.setNroCpf(cpfLimpo);

    return ClienteResponse.from(clienteRepository.save(cliente));
  }

  @Transactional
  public void deletar(Long id) {
    buscarEntidade(id);
    clienteRepository.deleteById(id);
  }

  public Cliente buscarEntidade(Long id) {
    return clienteRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + id));
  }

  public Cliente buscarOuCriarPorCpf(ClienteRequest request) {
    String cpfLimpo = limparCpf(request.getNroCpf());
    return clienteRepository.findByNroCpf(cpfLimpo)
      .orElseGet(() -> {
        Cliente novo = new Cliente();
        novo.setNmeCliente(request.getNmeCliente());
        novo.setDtaNascimento(request.getDtaNascimento());
        novo.setNroCpf(cpfLimpo);

        return clienteRepository.save(novo);
      });
  }

  private String limparCpf(String cpf) {
    return cpf.replaceAll("[^0-9]", "");
  }
}
