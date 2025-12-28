package com.example.testwork.service;

import com.example.testwork.DTO.ClientDTO;
import com.example.testwork.entity.Client;
import com.example.testwork.mapper.Mapper;
import com.example.testwork.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public List<ClientDTO> getAllClients() {
        List<Client> clients = clientRepository.findAll();
        return Mapper.INSTANCE.toDTOList(clients);
    }


    public ClientDTO getClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент не найден"));
        return Mapper.INSTANCE.toClientDTO(client);
    }

    @Transactional
    public ClientDTO addClient(ClientDTO clientDTO) {
        Client client = Client.builder()
                .name(clientDTO.getName())
                .phone(clientDTO.getPhone())
                .email(clientDTO.getEmail())
                .build();

        Client savedClient = clientRepository.save(client);
        return Mapper.INSTANCE.toClientDTO(savedClient);
    }

    @Transactional
    public ClientDTO updateClient(ClientDTO clientDTO) {
        Client client = clientRepository.findById(clientDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Клиент не найден"));

        // Проверяем уникальность телефона, если он изменился
        if (!client.getPhone().equals(clientDTO.getPhone())) {
            if (clientRepository.existsByPhone(clientDTO.getPhone())) {
                throw new IllegalArgumentException("Телефон уже используется другим клиентом");
            }
        }

        client.setName(clientDTO.getName());
        client.setPhone(clientDTO.getPhone());
        client.setEmail(clientDTO.getEmail());

        Client updatedClient = clientRepository.save(client);
        return Mapper.INSTANCE.toClientDTO(updatedClient);
    }

}