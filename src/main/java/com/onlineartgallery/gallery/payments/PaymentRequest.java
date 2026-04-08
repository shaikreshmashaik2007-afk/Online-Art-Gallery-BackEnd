package com.onlineartgallery.gallery.payments;

public class PaymentRequest {
    // amount in rupees (whole or decimal)
    private Double amount;

    public PaymentRequest() {}

    public PaymentRequest(Double amount) {
        this.amount = amount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
