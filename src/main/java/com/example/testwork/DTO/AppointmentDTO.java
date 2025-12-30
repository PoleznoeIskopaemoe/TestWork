package com.example.testwork.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentDTO {
    private UUID orderId;

    @NotNull(message = "ID клиента обязательно")
    private Long clientId;

    @NotNull(message = "Дата и время обязательны")
    @FutureOrPresent(message = "Дата должна быть текущей или будущей")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime datetime;
}