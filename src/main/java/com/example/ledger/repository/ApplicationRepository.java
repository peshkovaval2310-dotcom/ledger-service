package com.example.ledger.repository;

import com.example.ledger.model.Application;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository {
    Optional<Application> findById(UUID id);

    void save(Application application);
}
