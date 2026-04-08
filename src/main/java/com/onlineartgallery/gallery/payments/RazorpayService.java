package com.onlineartgallery.gallery.payments;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * RazorpayService handles all Razorpay API interactions.
 * Creates a singleton RazorpayClient instance for the application.
 */
@Service
public class RazorpayService {

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;

    private RazorpayClient razorpayClient;

    /**
     * Initialize RazorpayClient after Spring injects the properties.
     * This ensures we have a single RazorpayClient instance for the entire application.
     */
    @PostConstruct
    public void init() {
        try {
            // Debug logging - print the loaded values
            System.out.println("=".repeat(60));
            System.out.println("🔧 Initializing Razorpay Service...");
            System.out.println("📋 Key ID loaded: " + (keyId != null ? keyId : "NULL"));
            System.out.println("📋 Key Secret loaded: " + (keySecret != null && !keySecret.isEmpty() ? 
                keySecret.substring(0, 8) + "..." : "NULL"));
            System.out.println("=".repeat(60));

            // Validate credentials
            if (keyId == null || keyId.trim().isEmpty()) {
                throw new IllegalStateException(
                    "❌ Razorpay Key ID is NULL or empty! " +
                    "Check application.properties or set RAZORPAY_KEY_ID environment variable."
                );
            }

            if (keySecret == null || keySecret.trim().isEmpty()) {
                throw new IllegalStateException(
                    "❌ Razorpay Key Secret is NULL or empty! " +
                    "Check application.properties or set RAZORPAY_KEY_SECRET environment variable."
                );
            }

            // Create RazorpayClient
            this.razorpayClient = new RazorpayClient(keyId, keySecret);
            
            System.out.println("✅ Razorpay client initialized successfully!");
            System.out.println("✅ Using Key ID: " + keyId);
            System.out.println("=".repeat(60));
            
        } catch (RazorpayException e) {
            System.err.println("❌ Failed to initialize Razorpay client!");
            System.err.println("❌ Error: " + e.getMessage());
            System.err.println("❌ Key ID used: " + keyId);
            throw new IllegalStateException("Failed to initialize Razorpay client: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("❌ Unexpected error during Razorpay initialization!");
            System.err.println("❌ Error: " + e.getMessage());
            throw new IllegalStateException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Create a Razorpay order.
     * @param amountInRupees Amount in rupees (will be converted to paise)
     * @param receipt Receipt identifier
     * @return Razorpay Order object
     * @throws RazorpayException if order creation fails
     */
    public Order createOrder(double amountInRupees, String receipt) throws RazorpayException {
        if (razorpayClient == null) {
            throw new IllegalStateException("RazorpayClient is not initialized!");
        }

        // Convert rupees to paise (Razorpay uses smallest currency unit)
        long amountInPaise = Math.round(amountInRupees * 100);

        // Create order options
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);
        orderRequest.put("payment_capture", 1); // Auto capture

        System.out.println("📝 Creating Razorpay order:");
        System.out.println("   Amount: ₹" + amountInRupees + " (" + amountInPaise + " paise)");
        System.out.println("   Receipt: " + receipt);

        // Create order
        Order order = razorpayClient.orders.create(orderRequest);
        
        System.out.println("✅ Order created: " + order.get("id"));
        return order;
    }

    /**
     * Get the Razorpay Key ID for frontend.
     * @return Razorpay Key ID
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Get the Razorpay Key Secret (for signature verification).
     * @return Razorpay Key Secret
     */
    public String getKeySecret() {
        return keySecret;
    }

    /**
     * Check if the service is properly initialized.
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return razorpayClient != null && keyId != null && keySecret != null;
    }
}
