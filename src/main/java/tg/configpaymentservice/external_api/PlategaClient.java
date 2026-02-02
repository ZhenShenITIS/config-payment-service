package tg.configpaymentservice.external_api;


import tg.configpaymentservice.external_api.constants.PaymentStatus;
import tg.configpaymentservice.external_api.dto.CreatePaymentRequest;
import tg.configpaymentservice.external_api.dto.CreatePaymentResponse;

public interface PlategaClient {
    CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest);
    PaymentStatus updateStatus(String paymentId);

}
