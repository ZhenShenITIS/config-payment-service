package tg.configpaymentservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.configpaymentservice.constants.MessageText;
import tg.configpaymentservice.constants.PaymentResult;
import tg.configpaymentservice.constants.TopUpSource;
import tg.configpaymentservice.dto.ConfirmedPayment;
import tg.configpaymentservice.external_api.PlategaClient;
import tg.configpaymentservice.external_api.constants.PaymentStatus;
import tg.configpaymentservice.external_api.dto.CreatePaymentRequest;
import tg.configpaymentservice.external_api.dto.CreatePaymentResponse;
import tg.configpaymentservice.external_api.dto.PaymentDetails;
import tg.configpaymentservice.external_api.model.PlategaPayment;
import tg.configpaymentservice.external_api.repositories.PlategaPaymentRepository;
import tg.configpaymentservice.messaging.producers.PaymentProducer;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PlategaClient plategaClient;
    private final PlategaPaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;

    @Value("${TELEGRAM_BOT_USERNAME}")
    private String telegramBotUsername;


    public PlategaPayment createPlategaPayment(Long amount, int paymentMethodInt, Long userId) {
        CreatePaymentRequest createPaymentRequest = new CreatePaymentRequest(
                paymentMethodInt,
                new PaymentDetails(amount.doubleValue(), "RUB"),
                MessageText.PAYMENT_DESCRIPTION.getMessageText().formatted(userId),
                "https://t.me/" + telegramBotUsername,
                "https://t.me/" + telegramBotUsername,
                "...");
        CreatePaymentResponse createPaymentResponse = plategaClient.createPayment(createPaymentRequest);
        PlategaPayment plategaPayment = PlategaPayment
                .builder()
                .paymentDetails(createPaymentResponse.paymentDetails())
                .amount(Double.parseDouble(createPaymentResponse.paymentDetails().split(" ")[0]))
                .currency(createPaymentResponse.paymentDetails().split(" ")[1])
                .status(createPaymentResponse.status())
                .paymentMethod(createPaymentResponse.paymentMethod())
                .cryptoAmount(createPaymentResponse.cryptoAmount())
                .redirect(createPaymentResponse.redirect())
                .returnUrl(createPaymentResponse.returnUrl())
                .transactionId(createPaymentResponse.transactionId())
                .usdtRate(createPaymentResponse.usdtRate())
                .build();
        paymentRepository.save(plategaPayment);
        return plategaPayment;
    }


    @Transactional
    public PaymentResult checkPayment(String paymentId, Long userId) {
        PlategaPayment payment = paymentRepository.findByIdWithLock(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));


        if (!PaymentStatus.PENDING.equals(payment.getStatus())) {
            return PaymentResult.EXPIRED;
        }

        PaymentStatus remoteStatus;
        try {
            // TODO remove HTTP-request from @Transactional
            remoteStatus = plategaClient.updateStatus(paymentId);
        } catch (Exception e) {
            log.error("Error fetching status from Platega", e);
            return PaymentResult.PROCESSING;
        }

        if (payment.getStatus() != remoteStatus) {
            payment.setStatus(remoteStatus);
            paymentRepository.save(payment);

            if (remoteStatus == PaymentStatus.CONFIRMED) {
                paymentProducer.sendConfirmedPaymentToKafka(new ConfirmedPayment(userId, payment.getAmount().longValue(), paymentId));
                return PaymentResult.CONFIRMED;
            } else if (remoteStatus == PaymentStatus.CANCELED) {
                return PaymentResult.CANCELED;
            }
        }
        return PaymentResult.PROCESSING;
    }


}
