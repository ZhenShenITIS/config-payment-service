package tg.configpaymentservice.external_api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import tg.configpaymentservice.external_api.constants.PaymentMethod;
import tg.configpaymentservice.external_api.constants.PaymentStatus;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "platega_payments")
@ToString
public class PlategaPayment {
    @Id
    private String transactionId;
    private PaymentMethod paymentMethod;
    private String redirect;
    private String returnUrl;
    private String paymentDetails;
    private Double amount;
    private String currency;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private Double usdtRate;
    private Double cryptoAmount;

}
