package com.example.testwork;

import com.example.testwork.DTO.ClientDTO;
import com.example.testwork.entity.Client;
import com.example.testwork.repository.ClientRepository;
import com.example.testwork.service.ClientService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client testClient;
    private ClientDTO testClientDTO;

    @BeforeEach
    void setUp() {
        testClient = Client.builder()
                .id(1L)
                .name("Иван Иванов")
                .phone("+79161234567")
                .email("ivan@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testClientDTO = new ClientDTO();
        testClientDTO.setId(1L);
        testClientDTO.setName("Иван Иванов");
        testClientDTO.setPhone("+79161234567");
        testClientDTO.setEmail("ivan@example.com");
    }

    @Test
    void getAllClients_shouldReturnClientList() {

        List<Client> clients = Arrays.asList(testClient);
        when(clientRepository.findAll()).thenReturn(clients);

        List<ClientDTO> result = clientService.getAllClients();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Иван Иванов", result.get(0).getName());
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void getClient_withValidId_shouldReturnClient() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));

        ClientDTO result = clientService.getClient(1L);

        assertNotNull(result);
        assertEquals("Иван Иванов", result.getName());
        assertEquals("+79161234567", result.getPhone());
        verify(clientRepository, times(1)).findById(1L);
    }

    @Test
    void getClient_withInvalidId_shouldThrowException() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> clientService.getClient(99L)
        );

        assertEquals("Клиент не найден", exception.getMessage());
        verify(clientRepository, times(1)).findById(99L);
    }

    @Test
    void addClient_withValidData_shouldCreateClient() {
        ClientDTO newClientDTO = new ClientDTO();
        newClientDTO.setName("Петр Петров");
        newClientDTO.setPhone("+79161234568"); // Новый номер
        newClientDTO.setEmail("petr@example.com");

        Client savedClient = Client.builder()
                .id(2L)
                .name("Петр Петров")
                .phone("+79161234568")
                .email("petr@example.com")
                .build();

        when(clientRepository.existsByPhone("+79161234568")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        ClientDTO result = clientService.addClient(newClientDTO);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Петр Петров", result.getName());
        assertEquals("+79161234568", result.getPhone());

        verify(clientRepository, times(1)).existsByPhone("+79161234568");
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void addClient_withDuplicatePhone_shouldThrowException() {
        ClientDTO newClientDTO = new ClientDTO();
        newClientDTO.setName("Петр Петров");
        newClientDTO.setPhone("+79161234567"); // Дублирующий телефон
        newClientDTO.setEmail("petr@example.com");

        when(clientRepository.existsByPhone("+79161234567")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clientService.addClient(newClientDTO)
        );

        assertEquals("Клиент с таким телефоном уже существует", exception.getMessage());

        verify(clientRepository, times(1)).existsByPhone("+79161234567");
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void updateClient_withValidData_shouldUpdateClient() {
        ClientDTO updateDTO = new ClientDTO();
        updateDTO.setId(1L);
        updateDTO.setName("Иван Обновленный");
        updateDTO.setPhone("+79161234567"); // Тот же телефон, не меняется
        updateDTO.setEmail("ivan.new@example.com");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        ClientDTO result = clientService.updateClient(updateDTO);

        assertNotNull(result);
        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, never()).existsByPhone(anyString());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void updateClient_withNewDuplicatePhone_shouldThrowException() {
        ClientDTO updateDTO = new ClientDTO();
        updateDTO.setId(1L);
        updateDTO.setName("Иван Обновленный");
        updateDTO.setPhone("+79161234599"); // Новый телефон (дубликат)
        updateDTO.setEmail("ivan.new@example.com");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(clientRepository.existsByPhone("+79161234599")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clientService.updateClient(updateDTO)
        );

        assertEquals("Телефон уже используется другим клиентом", exception.getMessage());
        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).existsByPhone("+79161234599");
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void updateClient_withNewUniquePhone_shouldUpdateSuccessfully() {
        ClientDTO updateDTO = new ClientDTO();
        updateDTO.setId(1L);
        updateDTO.setName("Иван Обновленный");
        updateDTO.setPhone("+79161234599"); // Новый уникальный телефон
        updateDTO.setEmail("ivan.new@example.com");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(clientRepository.existsByPhone("+79161234599")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        ClientDTO result = clientService.updateClient(updateDTO);

        assertNotNull(result);
        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).existsByPhone("+79161234599");
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void updateClient_withNonExistingId_shouldThrowException() {
        ClientDTO updateDTO = new ClientDTO();
        updateDTO.setId(99L);
        updateDTO.setName("Не существующий");
        updateDTO.setPhone("+79161234567");

        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> clientService.updateClient(updateDTO)
        );

        assertEquals("Клиент не найден", exception.getMessage());
        verify(clientRepository, times(1)).findById(99L);
        verify(clientRepository, never()).existsByPhone(anyString());
        verify(clientRepository, never()).save(any(Client.class));
    }
}