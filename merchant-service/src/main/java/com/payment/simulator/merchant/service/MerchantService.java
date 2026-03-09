package com.payment.simulator.merchant.service;

import com.payment.simulator.merchant.entity.Merchant;
import com.payment.simulator.merchant.repository.MerchantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    public Merchant createMerchant(Merchant merchant) {
        if (merchantRepository.existsByEmail(merchant.getEmail())) {
            throw new RuntimeException("Merchant with this email already exists");
        }
        merchant.setApiKey(generateApiKey());
        return merchantRepository.save(merchant);
    }

    public Merchant getMerchant(Long id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Merchant not found: " + id));
    }

    public List<Merchant> getAllMerchants() {
        return merchantRepository.findAll();
    }

    public Merchant regenerateApiKey(Long id) {
        Merchant merchant = getMerchant(id);
        merchant.setApiKey(generateApiKey());
        return merchantRepository.save(merchant);
    }

    public boolean validateApiKey(String apiKey) {
        return merchantRepository.findByApiKey(apiKey).isPresent();
    }

    private String generateApiKey() {
        return "pk_" + UUID.randomUUID().toString().replace("-", "");
    }
}
