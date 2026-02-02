package tg.configpaymentservice.constants;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@AllArgsConstructor
@Getter
public enum DialogStageName {
    PROMO_CODE_INPUT("promo_input"),
    PAYMENT("payment"),
    CRYPTO_WITHDRAW_SUM("crypto_wd_sum"),
    ADD_REF_PROMO_INPUT("add_ref_promo"),
    DEVICE_INPUT("device_input"),
    CRYPTO_WITHDRAW_WALLET("crypto_wd_wallet"),
    NONE("none");

    private final String dialogStageName;
}
