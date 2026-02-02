package tg.configpaymentservice.dto;

public record ConfirmedPayment(
        Long userId,
        Long amount,
        String paymentId
) {
}
