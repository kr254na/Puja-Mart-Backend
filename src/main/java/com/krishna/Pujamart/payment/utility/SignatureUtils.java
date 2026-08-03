package com.krishna.Pujamart.payment.utility;

import com.razorpay.Utils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

@Slf4j
public final class SignatureUtils {

    private SignatureUtils() {}

    public static boolean verifyPaymentSignature(
            String orderId,
            String paymentId,
            String signature,
            String secret) {

        if (orderId == null || paymentId == null || signature == null || secret == null) {
            return false;
        }

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            return Utils.verifyPaymentSignature(options, secret);

        } catch (Exception e) {
            log.error("Failed to verify Razorpay signature", e);
            return false;
        }
    }

    public static boolean verifyWebhookSignature(
            String payload,
            String webhookSignature,
            String webhookSecret) {

        if (payload == null || webhookSignature == null || webhookSecret == null) {
            return false;
        }

        try {
            return Utils.verifyWebhookSignature(
                    payload,
                    webhookSignature,
                    webhookSecret
            );

        } catch (Exception e) {
            log.error("Webhook signature verification failed", e);
            return false;
        }
    }
}
