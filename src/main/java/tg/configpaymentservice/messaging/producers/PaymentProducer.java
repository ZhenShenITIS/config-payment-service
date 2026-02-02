package tg.configpaymentservice.messaging.producers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import tg.configpaymentservice.dto.ConfirmedPayment;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentProducer {

    private final StreamBridge streamBridge;

    public void sendConfirmedPaymentToKafka(ConfirmedPayment confirmedPayment) {
        Long userId = confirmedPayment.userId();

        Message<ConfirmedPayment> message = MessageBuilder
                .withPayload(confirmedPayment)
                .setHeader(KafkaHeaders.KEY, userId)
                .build();

        streamBridge.send("confirmedPayment-out-0", message);

        log.info("ConfirmedPayment sent to Kafka via StreamBridge: userId={}", userId);
    }
}
