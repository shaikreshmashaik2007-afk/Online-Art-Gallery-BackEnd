package com.onlineartgallery.gallery.payments;

import com.razorpay.Order;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

/**
 * PaymentController handles HTTP endpoints for Razorpay payment integration.
 * Delegates Razorpay operations to RazorpayService.
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
public class PaymentController {

    private final RazorpayService razorpayService;

    @Autowired
    public PaymentController(RazorpayService razorpayService) {
        this.razorpayService = razorpayService;
        System.out.println("✅ PaymentController initialized with RazorpayService");
    }

    /**
     * Create a Razorpay order for payment.
     * POST /api/payments/create-order
     * Request body: { "amount": 2500.00 }
     * Response: { "orderId": "order_xxx", "amount": 250000, "currency": "INR", "key": "rzp_test_xxx" }
     */
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody PaymentRequest request) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📥 Received create-order request");
        System.out.println("   Amount: " + request.getAmount());
        
        try {
            // Validate service initialization
            if (!razorpayService.isInitialized()) {
                System.err.println("❌ RazorpayService not initialized!");
                Map<String, String> error = new HashMap<>();
                error.put("error", "Payment service not initialized");
                error.put("message", "Razorpay configuration is missing");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }

            // Validate amount
            if (request.getAmount() == null || request.getAmount() <= 0) {
                System.err.println("❌ Invalid amount: " + request.getAmount());
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid amount");
                error.put("message", "Amount must be greater than 0");
                return ResponseEntity.badRequest().body(error);
            }

            double amountInRupees = request.getAmount();
            String receipt = "rcpt_" + System.currentTimeMillis();

            // Create order via RazorpayService
            Order order = razorpayService.createOrder(amountInRupees, receipt);
            
            // Build response with order details and key for frontend
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.get("id")); // Use "orderId" for clarity
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("key", razorpayService.getKeyId()); // Frontend needs this
            
            System.out.println("✅ Order created successfully!");
            System.out.println("   Order ID: " + order.get("id"));
            System.out.println("   Amount: ₹" + amountInRupees);
            System.out.println("   Key: " + razorpayService.getKeyId());
            System.out.println("=".repeat(60) + "\n");
            
            return ResponseEntity.ok(response);

        } catch (RazorpayException e) {
            System.err.println("❌ Razorpay API error: " + e.getMessage());
            System.err.println("=".repeat(60) + "\n");
            Map<String, String> error = new HashMap<>();
            error.put("error", "Razorpay API error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=".repeat(60) + "\n");
            Map<String, String> error = new HashMap<>();
            error.put("error", "Could not create order");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Verify Razorpay payment signature.
     * POST /api/payments/verify
     * Request body: { "razorpay_order_id": "order_xxx", "razorpay_payment_id": "pay_xxx", "razorpay_signature": "xxx" }
     * Response: { "verified": true, "success": true } or { "verified": false }
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerification verification) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📥 Received verify payment request");
        System.out.println("   Order ID: " + verification.getRazorpay_order_id());
        System.out.println("   Payment ID: " + verification.getRazorpay_payment_id());
        
        try {
            // Validate input
            if (verification.getRazorpay_order_id() == null || 
                verification.getRazorpay_payment_id() == null || 
                verification.getRazorpay_signature() == null) {
                
                System.err.println("❌ Missing required fields!");
                Map<String, Object> error = new HashMap<>();
                error.put("verified", false);
                error.put("error", "Missing required fields");
                return ResponseEntity.badRequest().body(error);
            }

            // Generate signature for verification
            String data = verification.getRazorpay_order_id() + "|" + verification.getRazorpay_payment_id();
            String generatedSignature = hmacSha256(data, razorpayService.getKeySecret());
            
            // Compare signatures
            boolean isValid = generatedSignature.equals(verification.getRazorpay_signature());
            
            Map<String, Object> response = new HashMap<>();
            response.put("verified", isValid);
            
            if (isValid) {
                response.put("success", true);
                response.put("message", "Payment verified successfully");
                System.out.println("✅ Payment verified successfully!");
                System.out.println("   Payment ID: " + verification.getRazorpay_payment_id());
                System.out.println("=".repeat(60) + "\n");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Signature verification failed");
                System.err.println("❌ Payment verification failed!");
                System.err.println("   Expected signature: " + generatedSignature);
                System.err.println("   Received signature: " + verification.getRazorpay_signature());
                System.err.println("=".repeat(60) + "\n");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            System.err.println("❌ Verification error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=".repeat(60) + "\n");
            Map<String, Object> error = new HashMap<>();
            error.put("verified", false);
            error.put("error", "Verification error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Generate HMAC SHA256 signature for verification.
     */
    private String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(raw);
    }
}
