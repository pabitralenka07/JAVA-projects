package com.portfolio.isp.model;

public enum PaymentMethod {
    CREDIT_CARD("Credit / Debit Card"),
    UPI("Instant UPI / QR Code"),
    NET_BANKING("Internet Banking"),
    CASH("Cash / Cheque at Branch");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
