package tg.configpaymentservice.external_api.dto;

public record PaymentDetails (
        Double amount,
        String currency
) {
}
