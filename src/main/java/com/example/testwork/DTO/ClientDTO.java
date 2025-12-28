package com.example.testwork.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDTO {
    private Long id;

    @NonNull
    private String name;

    @NonNull
    private String phone;

    private String email;
}
