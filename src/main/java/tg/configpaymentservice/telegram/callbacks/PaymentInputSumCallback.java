package tg.configpaymentservice.telegram.callbacks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import tg.configpaymentservice.constants.ButtonText;
import tg.configpaymentservice.constants.CallbackName;
import tg.configpaymentservice.constants.DialogStageName;
import tg.configpaymentservice.constants.MessageText;
import tg.configpaymentservice.dto.PaymentContext;
import tg.configpaymentservice.external_api.constants.PaymentMethod;
import tg.zhenshen_bot_utils.callbacks.Callback;
import tg.zhenshen_bot_utils.client.TelegramProxyClient;
import tg.zhenshen_bot_utils.state.manager.RedisStateManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentInputSumCallback implements Callback {
    private final RedisStateManager<Long, PaymentContext> paymentContextRedisStateManager;
    private final RedisStateManager<Long, DialogStageName> dialogStageNameRedisStateManager;

    private final TelegramProxyClient telegramProxyClient;
    @Override
    public String getCallbackIdentifier() {
        return CallbackName.PAYMENT_INPUT_SUM.getCallbackName();
    }

    @Override
    public void processCallback(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        long userId = callbackQuery.getFrom().getId();
        int paymentMethod = Integer.parseInt(callbackQuery.getData().split(":")[1]);
        PaymentContext paymentContext = new PaymentContext(PaymentMethod.fromIntMethod(paymentMethod));
        paymentContextRedisStateManager.put(userId, paymentContext);
        log.info("Put context={} for userId={}", paymentContext, userId);
        dialogStageNameRedisStateManager.put(userId, DialogStageName.PAYMENT);
        log.info("Put stage {} for userId={}", DialogStageName.PAYMENT.getDialogStageName(), userId);

        EditMessageText editMessageText = EditMessageText
                .builder()
                .text(MessageText.INPUT_SUM_PAYMENT.getMessageText())
                .chatId(chatId)
                .messageId(messageId)
                .parseMode("HTML")
                .replyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(new InlineKeyboardRow(
                                InlineKeyboardButton
                                        .builder()
                                        .text(ButtonText.BACK.getText())
                                        .callbackData(CallbackName.TOP_UP.getCallbackName())
                                        .build()
                        ))
                        .build())
                .build();
        telegramProxyClient.execute(editMessageText);
    }
}
