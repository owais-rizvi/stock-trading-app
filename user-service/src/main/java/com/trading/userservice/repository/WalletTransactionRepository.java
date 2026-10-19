package com.trading.userservice.repository;

import com.trading.userservice.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, String> {
    Optional<WalletTransaction> findByReference(String reference);
}
