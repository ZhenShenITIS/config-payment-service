package tg.configpaymentservice.external_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import tg.configpaymentservice.external_api.constants.PaymentMethod;
import tg.configpaymentservice.external_api.constants.PaymentStatus;

public record CreatePaymentResponse (
        String transactionId,
        PaymentMethod paymentMethod,
        String redirect,
        @JsonProperty("return")
        String returnUrl,
        String paymentDetails,
        PaymentStatus status,
        String expiresIn,
        String merchantId,
        Double usdtRate,
        Double cryptoAmount

) {
}
