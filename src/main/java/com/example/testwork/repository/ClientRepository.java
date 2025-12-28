package com.example.testwork.repository;

import com.example.testwork.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByPhone(String phone);
    List<Client> findByNameContainingIgnoreCase(String name);
    boolean existsByPhone(String phone);
}
