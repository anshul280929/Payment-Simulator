package com.payment.simulator.transaction.service;

import com.payment.simulator.common.dto.TransactionDTO;
import com.payment.simulator.common.dto.TransactionEventDTO;
import com.payment.simulator.common.enums.Currency;
import com.payment.simulator.common.enums.TransactionStatus;
import com.payment.simulator.common.event.PaymentEvent;
import com.payment.simulator.transaction.entity.Transaction;
import com.payment.simulator.transaction.entity.TransactionEvent;
import com.payment.simulator.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @RabbitListener(queues = "payment.authorized.queue")
    public void handleAuthorized(PaymentEvent event) {
        log.info("Received payment.authorized event: txn={}", event.getTransactionId());
        createOrUpdateTransaction(event, TransactionStatus.AUTHORIZED);
    }

    @RabbitListener(queues = "payment.captured.queue")
    public void handleCaptured(PaymentEvent event) {
        log.info("Received payment.captured event: txn={}", event.getTransactionId());
        updateTransactionStatus(event.getTransactionId(), TransactionStatus.CAPTURED,
                event.getAmount(), "Payment captured");
    }

    @RabbitListener(queues = "payment.refunded.queue")
    public void handleRefunded(PaymentEvent event) {
        log.info("Received payment.refunded event: txn={}", event.getTransactionId());
        TransactionStatus newStatus = TransactionStatus.valueOf(event.getStatus());
        updateTransactionStatus(event.getTransactionId(), newStatus,
                event.getAmount(), "Payment refunded");
    }

    @Transactional
    public void createOrUpdateTransaction(PaymentEvent event, TransactionStatus status) {
        Transaction txn = transactionRepository.findByTransactionId(event.getTransactionId())
                .orElseGet(() -> {
                    Transaction newTxn = new Transaction();
                    newTxn.setTransactionId(event.getTransactionId());
                    newTxn.setMerchantId(event.getMerchantId());
                    newTxn.setAmount(event.getAmount());
                    newTxn.setCurrency(Currency.valueOf(event.getCurrency()));
                    return newTxn;
                });

        TransactionStatus oldStatus = txn.getStatus();
        txn.setStatus(status);
        txn.addEvent(oldStatus, status, "Transaction " + status.name().toLowerCase(), "SYSTEM");
        transactionRepository.save(txn);
    }

    @Transactional
    public void updateTransactionStatus(String transactionId, TransactionStatus newStatus,
                                        BigDecimal amount, String description) {
        Transaction txn = transactionRepository.findByTransactionId(transactionId).orElse(null);
        if (txn == null) {
            log.warn("Transaction not found for event: {}", transactionId);
            return;
        }

        TransactionStatus oldStatus = txn.getStatus();
        txn.setStatus(newStatus);
        if (newStatus == TransactionStatus.CAPTURED) {
            txn.setCapturedAmount(amount);
        } else if (newStatus == TransactionStatus.REFUNDED || newStatus == TransactionStatus.PARTIALLY_REFUNDED) {
            txn.setRefundedAmount(txn.getRefundedAmount().add(amount));
        }
        txn.addEvent(oldStatus, newStatus, description, "SYSTEM");
        transactionRepository.save(txn);
    }

    public TransactionDTO getTransaction(String transactionId) {
        Transaction txn = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        return toDTO(txn);
    }

    public List<TransactionDTO> searchTransactions(Long merchantId, TransactionStatus status) {
        List<Transaction> transactions;
        if (merchantId != null && status != null) {
            transactions = transactionRepository.findByMerchantIdAndStatus(merchantId, status);
        } else if (merchantId != null) {
            transactions = transactionRepository.findByMerchantId(merchantId);
        } else if (status != null) {
            transactions = transactionRepository.findByStatus(status);
        } else {
            transactions = transactionRepository.findAll();
        }
        return transactions.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public TransactionDTO initiateChargeback(String transactionId) {
        Transaction txn = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

        if (txn.getStatus() != TransactionStatus.CAPTURED &&
            txn.getStatus() != TransactionStatus.SETTLED) {
            throw new RuntimeException("Cannot chargeback transaction in status: " + txn.getStatus());
        }

        TransactionStatus oldStatus = txn.getStatus();
        txn.setStatus(TransactionStatus.CHARGEBACK_INITIATED);
        txn.addEvent(oldStatus, TransactionStatus.CHARGEBACK_INITIATED,
                "Chargeback initiated by cardholder", "SYSTEM");
        transactionRepository.save(txn);

        log.info("Chargeback initiated for txn={}", transactionId);
        return toDTO(txn);
    }

    private TransactionDTO toDTO(Transaction txn) {
        TransactionDTO dto = new TransactionDTO();
        dto.setTransactionId(txn.getTransactionId());
        dto.setMerchantId(txn.getMerchantId());
        dto.setAmount(txn.getAmount());
        dto.setCapturedAmount(txn.getCapturedAmount());
        dto.setRefundedAmount(txn.getRefundedAmount());
        dto.setCurrency(txn.getCurrency());
        dto.setStatus(txn.getStatus());
        dto.setMaskedCardNumber(txn.getMaskedCardNumber());
        dto.setCardToken(txn.getCardToken());
        dto.setAuthorizationCode(txn.getAuthorizationCode());
        dto.setCreatedAt(txn.getCreatedAt());
        dto.setUpdatedAt(txn.getUpdatedAt());
        dto.setEvents(txn.getEvents().stream().map(this::toEventDTO).collect(Collectors.toList()));
        return dto;
    }

    private TransactionEventDTO toEventDTO(TransactionEvent event) {
        TransactionEventDTO dto = new TransactionEventDTO();
        dto.setId(event.getId());
        dto.setFromStatus(event.getFromStatus());
        dto.setToStatus(event.getToStatus());
        dto.setDescription(event.getDescription());
        dto.setPerformedBy(event.getPerformedBy());
        dto.setTimestamp(event.getTimestamp());
        return dto;
    }
}
