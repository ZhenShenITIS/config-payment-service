package tg.configpaymentservice.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TelegramMethodName {
    EDIT_MESSAGE_TEXT("editMessageText"),
    SEND_MESSAGE("sendMessage");

    private final String name;
}
