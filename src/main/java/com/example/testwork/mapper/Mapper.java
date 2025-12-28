package com.example.testwork.mapper;

import com.example.testwork.DTO.ClientDTO;
import com.example.testwork.entity.Client;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@org.mapstruct.Mapper(componentModel = "spring")
@Component
public interface Mapper {

    Mapper INSTANCE = Mappers.getMapper(Mapper.class);

    ClientDTO toClientDTO(Client client);
    List<ClientDTO> toDTOList(List<Client> clients);
}
