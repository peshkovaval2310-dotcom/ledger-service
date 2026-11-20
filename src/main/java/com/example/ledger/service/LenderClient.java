package com.example.ledger.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface LenderClient {
    LenderBlockResult blockLimit(
            UUID lenderId,
            UUID applicationId,
            BigDecimal amount,
            String requestId
    );
}
