package com.payment.simulator.bank.service;

import com.payment.simulator.bank.entity.AuthorizationLog;
import com.payment.simulator.bank.entity.BankAccount;
import com.payment.simulator.bank.repository.AuthorizationLogRepository;
import com.payment.simulator.bank.repository.BankAccountRepository;
import com.payment.simulator.common.dto.BankAuthRequest;
import com.payment.simulator.common.dto.BankAuthResponse;
import com.payment.simulator.common.util.CardMasker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

@Service
public class BankService {

    private static final Logger log = LoggerFactory.getLogger(BankService.class);
    private static final SecureRandom random = new SecureRandom();

    private final BankAccountRepository bankAccountRepository;
    private final AuthorizationLogRepository authLogRepository;

    public BankService(BankAccountRepository bankAccountRepository,
                       AuthorizationLogRepository authLogRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.authLogRepository = authLogRepository;
    }

    @Transactional
    public BankAuthResponse authorize(BankAuthRequest request) {
        String last4 = CardMasker.lastFour(request.getCardNumber());
        log.info("Processing bank authorization for card ****{}", last4);

        // Simulated decline rules based on card number suffix
        if (request.getCardNumber().endsWith("0000")) {
            return logAndReturn(last4, "AUTHORIZE", request.getAmount(),
                    BankAuthResponse.declined("Insufficient funds", "51"));
        }
        if (request.getCardNumber().endsWith("1111")) {
            return logAndReturn(last4, "AUTHORIZE", request.getAmount(),
                    BankAuthResponse.declined("Card expired", "54"));
        }
        if (request.getCardNumber().endsWith("9999")) {
            // Simulate timeout with delay
            try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return logAndReturn(last4, "AUTHORIZE", request.getAmount(),
                    BankAuthResponse.declined("Transaction timeout", "91"));
        }

        // Random 5% decline for realism
        if (random.nextInt(100) < 5) {
            return logAndReturn(last4, "AUTHORIZE", request.getAmount(),
                    BankAuthResponse.declined("Do not honor", "05"));
        }

        // Check account balance (if account exists)
        Optional<BankAccount> accountOpt = bankAccountRepository.findByCardNumber(request.getCardNumber());
        if (accountOpt.isPresent()) {
            BankAccount account = accountOpt.get();
            if (!"ACTIVE".equals(account.getStatus())) {
                return logAndReturn(last4, "AUTHORIZE", request.getAmount(),
                        BankAuthResponse.declined("Card inactive", "62"));
            }
            if (account.getBalance().compareTo(request.getAmount()) < 0) {
                return logAndReturn(last4, "AUTHORIZE", request.getAmount(),
                        BankAuthResponse.declined("Insufficient funds", "51"));
            }
        }

        // Approve the transaction
        String authCode = "AUTH" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return logAndReturn(last4, "AUTHORIZE", request.getAmount(),
                BankAuthResponse.approved(authCode));
    }

    @Transactional
    public BankAuthResponse capture(BankAuthRequest request) {
        String cardNumber = request.getCardNumber();
        String last4 = CardMasker.lastFour(cardNumber);
        log.info("Processing bank capture for card ****{}, amount={}", last4, request.getAmount());

        Optional<BankAccount> accountOpt = bankAccountRepository.findByCardNumber(cardNumber);
        if (accountOpt.isPresent()) {
            BankAccount account = accountOpt.get();
            account.setBalance(account.getBalance().subtract(request.getAmount()));
            bankAccountRepository.save(account);
        }

        String authCode = "CAP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return logAndReturn(last4, "CAPTURE", request.getAmount(),
                BankAuthResponse.approved(authCode));
    }

    @Transactional
    public BankAuthResponse refund(BankAuthRequest request) {
        String cardNumber = request.getCardNumber();
        String last4 = CardMasker.lastFour(cardNumber);
        log.info("Processing bank refund for card ****{}, amount={}", last4, request.getAmount());

        Optional<BankAccount> accountOpt = bankAccountRepository.findByCardNumber(cardNumber);
        if (accountOpt.isPresent()) {
            BankAccount account = accountOpt.get();
            account.setBalance(account.getBalance().add(request.getAmount()));
            bankAccountRepository.save(account);
        }

        String authCode = "REF" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return logAndReturn(last4, "REFUND", request.getAmount(),
                BankAuthResponse.approved(authCode));
    }

    private BankAuthResponse logAndReturn(String last4, String action, BigDecimal amount,
                                          BankAuthResponse response) {
        AuthorizationLog authLog = new AuthorizationLog();
        authLog.setCardNumberLast4(last4);
        authLog.setAction(action);
        authLog.setAmount(amount);
        authLog.setApproved(response.isApproved());
        authLog.setAuthorizationCode(response.getAuthorizationCode());
        authLog.setDeclineReason(response.getDeclineReason());
        authLog.setResponseCode(response.getResponseCode());
        authLogRepository.save(authLog);
        return response;
    }
}
