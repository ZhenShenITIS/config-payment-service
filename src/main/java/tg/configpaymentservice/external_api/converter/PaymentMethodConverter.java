package tg.configpaymentservice.external_api.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tg.configpaymentservice.external_api.constants.PaymentMethod;

@Converter(autoApply = true)
public class PaymentMethodConverter implements AttributeConverter<PaymentMethod, String> {

    @Override
    public String convertToDatabaseColumn(PaymentMethod attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getTextMethod();
    }

    @Override
    public PaymentMethod convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return PaymentMethod.fromTextMethod(dbData);
    }
}
