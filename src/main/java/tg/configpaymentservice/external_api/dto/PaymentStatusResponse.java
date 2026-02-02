package tg.configpaymentservice.external_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tg.configpaymentservice.external_api.constants.PaymentStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentStatusResponse(
        String id,
        PaymentStatus status
) {

}
