package tg.configpaymentservice.external_api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tg.configpaymentservice.external_api.constants.PaymentStatus;
import tg.configpaymentservice.external_api.dto.CreatePaymentRequest;
import tg.configpaymentservice.external_api.dto.CreatePaymentResponse;
import tg.configpaymentservice.external_api.dto.PaymentStatusResponse;

@Component
@RequiredArgsConstructor
public class PlategaClientImpl implements PlategaClient {
    private final RestClient plategaRestClient;
    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest) {
        return plategaRestClient.post()
                .uri("/transaction/process")
                .body(createPaymentRequest)
                .retrieve()
                .body(CreatePaymentResponse.class);
    }

    @Override
    public PaymentStatus updateStatus(String paymentId) {
        PaymentStatusResponse response = plategaRestClient.get()
                .uri("/transaction/{paymentId}", paymentId)
                .retrieve()
                .body(PaymentStatusResponse.class);
        return response.status();
    }
}
