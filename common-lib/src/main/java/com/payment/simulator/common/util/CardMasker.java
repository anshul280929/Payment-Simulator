package com.payment.simulator.common.util;

public class CardMasker {

    private CardMasker() {}

    /**
     * Masks a card number, showing only last 4 digits.
     * Input: "4111111111111234" → Output: "**** **** **** 1234"
     */
    public static String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String cleaned = cardNumber.replaceAll("\\s+", "");
        String last4 = cleaned.substring(cleaned.length() - 4);
        return "**** **** **** " + last4;
    }

    /**
     * Extracts the last 4 digits of a card number.
     */
    public static String lastFour(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "0000";
        }
        String cleaned = cardNumber.replaceAll("\\s+", "");
        return cleaned.substring(cleaned.length() - 4);
    }
}
