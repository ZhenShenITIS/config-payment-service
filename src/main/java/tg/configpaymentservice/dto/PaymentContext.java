package tg.configpaymentservice.dto;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tg.configpaymentservice.external_api.constants.PaymentMethod;
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public record PaymentContext(
        String paymentMethodName,
        long lastCheckTime
) {
    public PaymentContext(PaymentMethod paymentMethod) {
        this(paymentMethod.name(), 0);
    }

    public PaymentMethod paymentMethod() {
        return PaymentMethod.valueOf(paymentMethodName);
    }
}
