package com.onlineartgallery.gallery.payments;

import java.util.HashMap;
import java.util.Map;

public class PaymentVerification {
    private String razorpay_order_id;
    private String razorpay_payment_id;
    private String razorpay_signature;

    public PaymentVerification() {}

    public String getRazorpay_order_id() {
        return razorpay_order_id;
    }

    public void setRazorpay_order_id(String razorpay_order_id) {
        this.razorpay_order_id = razorpay_order_id;
    }

    public String getRazorpay_payment_id() {
        return razorpay_payment_id;
    }

    public void setRazorpay_payment_id(String razorpay_payment_id) {
        this.razorpay_payment_id = razorpay_payment_id;
    }

    public String getRazorpay_signature() {
        return razorpay_signature;
    }

    public void setRazorpay_signature(String razorpay_signature) {
        this.razorpay_signature = razorpay_signature;
    }

    public Map<String, String> toMap() {
        Map<String, String> m = new HashMap<>();
        m.put("razorpay_order_id", razorpay_order_id);
        m.put("razorpay_payment_id", razorpay_payment_id);
        m.put("razorpay_signature", razorpay_signature);
        return m;
    }
}
