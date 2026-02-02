package tg.configpaymentservice.telegram.callbacks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import tg.configpaymentservice.constants.ButtonText;
import tg.configpaymentservice.constants.CallbackName;
import tg.configpaymentservice.constants.MessageText;
import tg.configpaymentservice.constants.PaymentResult;
import tg.configpaymentservice.dto.PaymentContext;
import tg.configpaymentservice.services.PaymentService;
import tg.zhenshen_bot_utils.callbacks.Callback;
import tg.zhenshen_bot_utils.client.TelegramProxyClient;
import tg.zhenshen_bot_utils.state.manager.RedisStateManager;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckStatusPaymentCallback implements Callback {
    public static final String PAYLOAD_SEPARATOR = ":";
    private final TelegramProxyClient telegramProxyClient;
    private final PaymentService paymentService;
    private final RedisStateManager<Long, PaymentContext> paymentContextRedisStateManager;

    private final long DELAY_BETWEEN_CHECKS = 30_000;

    @Override
    public String getCallbackIdentifier() {
        return CallbackName.CHECK_STATUS_PAYMENT.getCallbackName();
    }

    @Override
    public void processCallback(CallbackQuery callbackQuery) {
        long userId = callbackQuery.getFrom().getId();
        String callbackId = callbackQuery.getId();
        long now = System.currentTimeMillis();
        Optional<PaymentContext> paymentContext = paymentContextRedisStateManager.get(userId);
        if (!paymentContext.isPresent()) {
            throw new RuntimeException("Context didn't found!");
        }
        if (!(now - DELAY_BETWEEN_CHECKS > paymentContext.get().lastCheckTime())) {
            answerQuery(callbackId, MessageText.PROCESSING_PAY.getMessageText());
            log.info("Requests for payment status updates are too frequent. UserId: {}", userId);
            return;
        }
        paymentContextRedisStateManager.put(userId, new PaymentContext(paymentContext.get().paymentMethodName(), now));
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();

        String data = callbackQuery.getData();

        String[] parts = data.split(PAYLOAD_SEPARATOR);

        if (parts.length < 2) {
            return;
        }

        String transactionId = parts[1];

        PaymentResult paymentResult = paymentService.checkPayment(transactionId, userId);
        log.info("Send request to payment service. Result: {}, userId: {}", paymentResult, userId);

        switch (paymentResult) {
            case CONFIRMED ->
                    editMessage(chatId, messageId, MessageText.CONFIRMED_PAY.getMessageText());
            case CANCELED -> editMessage(chatId, messageId, MessageText.CANCELED_PAY.getMessageText());
            case PROCESSING -> answerQuery(callbackId, MessageText.PROCESSING_PAY.getMessageText());
            case EXPIRED -> answerQuery(callbackId, MessageText.EXPIRED_PAY.getMessageText());

        }


    }

    private void editMessage(long chatId, int messageId, String text) {
        EditMessageText editMessageText = EditMessageText
                .builder()
                .text(text)
                .chatId(chatId)
                .parseMode("HTML")
                .replyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(new InlineKeyboardRow(
                                InlineKeyboardButton
                                        .builder()
                                        .text(ButtonText.BACK_TO_MENU.getText())
                                        .callbackData(CallbackName.BACK_TO_MENU.getCallbackName())
                                        .build()

                        ))
                        .build())
                .messageId(messageId)
                .build();
        telegramProxyClient.execute(editMessageText);
    }

    private void answerQuery(String callbackId, String text) {
        AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery
                .builder()
                .text(text)
                .callbackQueryId(callbackId)
                .showAlert(true)
                .build();

        telegramProxyClient.execute(answerCallbackQuery);

    }


}
