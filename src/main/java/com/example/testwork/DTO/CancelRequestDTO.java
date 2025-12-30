package com.example.testwork.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CancelRequestDTO {
    @NotNull(message = "ID клиента обязательно")
    private Long clientId;

    @NotNull(message = "ID записи обязательно")
    private UUID orderId;
}