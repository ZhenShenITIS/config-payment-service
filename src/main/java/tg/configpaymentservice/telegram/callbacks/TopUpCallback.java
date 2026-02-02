package tg.configpaymentservice.telegram.callbacks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import tg.configpaymentservice.constants.ButtonText;
import tg.configpaymentservice.constants.CallbackName;
import tg.configpaymentservice.constants.MessageText;
import tg.configpaymentservice.external_api.constants.PaymentMethod;
import tg.zhenshen_bot_utils.callbacks.Callback;
import tg.zhenshen_bot_utils.client.TelegramProxyClient;


import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TopUpCallback implements Callback {
    private final TelegramProxyClient telegramProxyClient;

    private final String PAYLOAD_SEPARATOR = ":";

    @Override
    public String getCallbackIdentifier() {
        return CallbackName.TOP_UP.getCallbackName();
    }

    @Override
    public void processCallback(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(ButtonText.PAYMENT_METHOD_SBP.getText())
                        .callbackData(CallbackName.PAYMENT_INPUT_SUM.getCallbackName() + PAYLOAD_SEPARATOR + PaymentMethod.SBP.getIntMethod())
                        .build()
        ));
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(ButtonText.PAYMENT_METHOD_CARD.getText())
                        .callbackData(CallbackName.PAYMENT_INPUT_SUM.getCallbackName() + PAYLOAD_SEPARATOR + PaymentMethod.CARD.getIntMethod())
                        .build()
        ));
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(ButtonText.BACK.getText())
                        .callbackData(CallbackName.BALANCE.getCallbackName())
                        .build()
        ));
        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(MessageText.CHOOSE_PAYMENT_METHOD.getMessageText())
                .parseMode("HTML")
                .replyMarkup(new InlineKeyboardMarkup(rows))
                .build();

        telegramProxyClient.execute(editMessage);
    }
}
