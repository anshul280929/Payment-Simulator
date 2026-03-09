package com.payment.simulator.gateway.service;

import com.payment.simulator.common.util.CardMasker;
import com.payment.simulator.common.util.EncryptionUtil;
import com.payment.simulator.gateway.entity.CardToken;
import com.payment.simulator.gateway.repository.CardTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TokenizationService {

    private static final Logger log = LoggerFactory.getLogger(TokenizationService.class);

    private final CardTokenRepository cardTokenRepository;
    private final EncryptionUtil encryptionUtil;

    public TokenizationService(CardTokenRepository cardTokenRepository, EncryptionUtil encryptionUtil) {
        this.cardTokenRepository = cardTokenRepository;
        this.encryptionUtil = encryptionUtil;
    }

    public CardToken tokenize(String cardNumber, String cardholderName,
                              String expiryMonth, String expiryYear) {
        CardToken cardToken = new CardToken();
        cardToken.setToken("tok_" + UUID.randomUUID().toString().replace("-", ""));
        cardToken.setEncryptedCardNumber(encryptionUtil.encrypt(cardNumber));
        cardToken.setLast4Digits(CardMasker.lastFour(cardNumber));
        cardToken.setCardholderName(cardholderName);
        cardToken.setExpiryMonth(expiryMonth);
        cardToken.setExpiryYear(expiryYear);

        CardToken saved = cardTokenRepository.save(cardToken);
        log.info("Card tokenized: token={}, last4={}", saved.getToken(), saved.getLast4Digits());
        return saved;
    }

    public String decryptCardNumber(String token) {
        CardToken cardToken = cardTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Card token not found"));
        return encryptionUtil.decrypt(cardToken.getEncryptedCardNumber());
    }
}
