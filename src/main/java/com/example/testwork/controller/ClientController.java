package com.example.testwork.controller;

import com.example.testwork.DTO.ClientDTO;
import com.example.testwork.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v0/pool/client/")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping("all")
    public List<ClientDTO> getAllClient(){
        return clientService.getAllClients();
    }

    @GetMapping("get/{id}")
    public ClientDTO getClientById(@PathVariable Long id){
        return clientService.getClient(id);
    }

    @PostMapping("add")
    public void  addClient(@RequestBody ClientDTO clientDTO){
        clientService.addClient(clientDTO);
    }

    @PostMapping("update")
    public void  updateClient(@RequestBody ClientDTO clientDTO){
        clientService.updateClient(clientDTO);
    }

}
