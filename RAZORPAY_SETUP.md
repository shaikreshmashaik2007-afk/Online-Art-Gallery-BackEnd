# Razorpay Payment Integration Setup

## 🔐 Security Notice

**IMPORTANT:** The test credentials in `application.properties` are for development only. 

⚠️ **DO NOT commit real/production Razorpay keys to version control!**

## Current Test Credentials

```properties
razorpay.key_id=rzp_test_RcKaD36uYcqBWS
razorpay.key_secret=UuLphOT4TVE53yBY5OjR9PGf
```

These are test mode credentials. Payments will be simulated.

## 🚀 Production Setup (Recommended)

### Option 1: Environment Variables (Recommended)

1. Set environment variables before running the application:

**Windows (PowerShell):**
```powershell
$env:RAZORPAY_KEY_ID="your_live_key_id"
$env:RAZORPAY_KEY_SECRET="your_live_key_secret"
```

**Linux/Mac:**
```bash
export RAZORPAY_KEY_ID=your_live_key_id
export RAZORPAY_KEY_SECRET=your_live_key_secret
```

2. The application will automatically use environment variables if they exist, falling back to `application.properties` values.

### Option 2: External Configuration File

1. Create `application-prod.properties` (this file is gitignored):
```properties
razorpay.key_id=your_live_key_id
razorpay.key_secret=your_live_key_secret
```

2. Run with production profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Option 3: Cloud Secrets Manager

For production deployments, use:
- **AWS:** AWS Secrets Manager or Parameter Store
- **Azure:** Azure Key Vault
- **GCP:** Secret Manager
- **Kubernetes:** Kubernetes Secrets

## 🧪 Testing with Test Mode

1. Use the test credentials in `application.properties`
2. Use Razorpay test cards:
   - **Success:** 4111 1111 1111 1111
   - **Failure:** 4000 0000 0000 0002
   - Any future CVV and expiry date

3. Test the flow:
   - Click "Buy Now" on any artwork
   - Razorpay checkout opens
   - Enter test card details
   - Payment succeeds/fails
   - Backend verifies the signature

## 📋 Endpoints

### Create Order
**POST** `/api/payments/create-order`

Request:
```json
{
  "amount": 2500.00
}
```

Response:
```json
{
  "id": "order_xxx",
  "amount": 250000,
  "currency": "INR",
  "key": "rzp_test_xxx"
}
```

### Verify Payment
**POST** `/api/payments/verify`

Request:
```json
{
  "razorpay_order_id": "order_xxx",
  "razorpay_payment_id": "pay_xxx",
  "razorpay_signature": "signature_xxx"
}
```

Response:
```json
{
  "verified": true,
  "success": true,
  "message": "Payment verified successfully"
}
```

## 🔧 Configuration Details

The application uses Spring's property placeholder with defaults:

```properties
razorpay.key_id=${RAZORPAY_KEY_ID:rzp_test_RcKaD36uYcqBWS}
razorpay.key_secret=${RAZORPAY_KEY_SECRET:UuLphOT4TVE53yBY5OjR9PGf}
```

This means:
1. Try to read from `RAZORPAY_KEY_ID` environment variable
2. If not found, use the test value `rzp_test_RcKaD36uYcqBWS`

## 🔄 Key Rotation

If you accidentally committed real keys:

1. **Immediately** rotate your keys in Razorpay Dashboard
2. Update environment variables with new keys
3. Remove keys from git history:
   ```bash
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch src/main/resources/application.properties" \
     --prune-empty --tag-name-filter cat -- --all
   ```

## 📚 Additional Resources

- [Razorpay Documentation](https://razorpay.com/docs/)
- [Razorpay Java SDK](https://github.com/razorpay/razorpay-java)
- [Payment Gateway Best Practices](https://razorpay.com/docs/payments/server-integration/best-practices/)

## ✅ Checklist Before Going Live

- [ ] Replace test keys with live keys via environment variables
- [ ] Enable webhook endpoints for payment notifications
- [ ] Add proper error handling and logging
- [ ] Implement idempotency for order creation
- [ ] Set up monitoring and alerts
- [ ] Test with real bank accounts (small amounts)
- [ ] Verify PCI compliance requirements
- [ ] Set up refund workflow
- [ ] Document payment reconciliation process
- [ ] Configure CORS for production domain
