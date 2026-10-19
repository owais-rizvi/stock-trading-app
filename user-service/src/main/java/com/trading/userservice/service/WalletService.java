package com.trading.userservice.service;

import com.trading.userservice.dto.UserResponse;
import com.trading.userservice.entity.TxType;
import com.trading.userservice.entity.User;
import com.trading.userservice.entity.WalletTransaction;
import com.trading.userservice.repository.UserRepository;
import com.trading.userservice.repository.WalletTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class WalletService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final WalletTransactionRepository walletTransactionRepository;

    private void record(String userId, String reference, TxType type, BigDecimal amount) {
        walletTransactionRepository.save(new WalletTransaction(null, userId, reference, type, amount, null));
    }

    @Transactional
    public UserResponse addFunds(String userId, String reference, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }
        User user = userRepository.findWithLockById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if(walletTransactionRepository.findByReference(reference).isPresent()) {
            return userService.mapToResponse(user);
        }

        user.setWalletBalance(user.getWalletBalance().add(amount));
        User savedUser = userRepository.save(user);

        record(userId, reference, TxType.CREDIT, amount);

        log.info("Funds added: {} to user: {}", amount, userId);
        return userService.mapToResponse(savedUser);
    }

    @Transactional
    public UserResponse deductFunds(String userId, String reference, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }
        User user = userRepository.findWithLockById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if(walletTransactionRepository.findByReference(reference).isPresent()) {
            return userService.mapToResponse(user);
        }

        if (user.getWalletBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance: " + user.getWalletBalance());
        }
        user.setWalletBalance(user.getWalletBalance().subtract(amount));
        User savedUser = userRepository.save(user);

        record(userId, reference, TxType.DEBIT, amount);

        log.info("Funds deducted: {} from user: {}", amount, userId);
        return userService.mapToResponse(savedUser);
    }

    @Transactional
    public UserResponse creditFunds(String userId, String reference, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        User user = userRepository.findWithLockById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if(walletTransactionRepository.findByReference(reference).isPresent()) {
            return userService.mapToResponse(user);
        }

        user.setWalletBalance(user.getWalletBalance().add(amount));
        User savedUser = userRepository.save(user);

        record (userId, reference, TxType.CREDIT, amount);

        log.info("Funds credited: {} to user: {}", amount, userId);
        return userService.mapToResponse(savedUser);
    }

    @Transactional
    public UserResponse reverse(String userId, String reference) {
        User user = userRepository.findWithLockById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        String reversalRef = reference + ":REVERSAL";
        Optional<WalletTransaction> original = walletTransactionRepository.findByReference(reference);

        // nothing to undo, or already undone
        if (original.isEmpty() || walletTransactionRepository.findByReference(reversalRef).isPresent()) {
            return userService.mapToResponse(user);
        }
        WalletTransaction tx = original.get();
        if (!tx.getUserId().equals(userId)) {
            throw new RuntimeException("Reference belongs to another user");
        }

        if (tx.getType() == TxType.DEBIT) {
            user.setWalletBalance(user.getWalletBalance().add(tx.getAmount()));
            record(userId, reversalRef, TxType.CREDIT, tx.getAmount());
        } else {
            if (user.getWalletBalance().compareTo(tx.getAmount()) < 0) {
                throw new RuntimeException( "Insufficient funds to reverse credit");
            }
            user.setWalletBalance(user.getWalletBalance().subtract(tx.getAmount()));
            record(userId, reversalRef, TxType.DEBIT, tx.getAmount());
        }
        userRepository.save(user);
        return userService.mapToResponse(user);
    }

}
