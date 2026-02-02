package tg.configpaymentservice.telegram.stages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import tg.configpaymentservice.constants.ButtonText;
import tg.configpaymentservice.constants.CallbackName;
import tg.configpaymentservice.constants.DialogStageName;
import tg.configpaymentservice.constants.MessageText;
import tg.configpaymentservice.dto.PaymentContext;
import tg.configpaymentservice.external_api.model.PlategaPayment;
import tg.configpaymentservice.services.PaymentService;
import tg.configpaymentservice.telegram.callbacks.TopUpCallback;
import tg.zhenshen_bot_utils.client.TelegramProxyClient;
import tg.zhenshen_bot_utils.stages.DialogStage;
import tg.zhenshen_bot_utils.state.manager.RedisStateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStage implements DialogStage {
    private final RedisStateManager<Long, DialogStageName> dialogStageNameRedisStateManager;
    private final RedisStateManager<Long, PaymentContext> paymentContextRedisStateManager;
    private final TopUpCallback topUpCallback;
    private final PaymentService paymentService;
    private final TelegramProxyClient telegramClient;

    public static final String PAYLOAD_SEPARATOR = ":";
    public static final long MIN_PAY_AMOUNT = 10;
    public static final long MAX_PAY_AMOUNT = 10000;


    @Override
    public String getDialogStageIdentifier() {
        return DialogStageName.PAYMENT.getDialogStageName();
    }

    @Override
    public void processCallbackQuery(CallbackQuery callbackQuery) {
        if (CallbackName.TOP_UP.getCallbackName().equals(callbackQuery.getData())) {
            dialogStageNameRedisStateManager.delete(callbackQuery.getFrom().getId());
            topUpCallback.processCallback(callbackQuery);
            return;
        }
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId())
                .text(MessageText.INPUT_SUM_PAYMENT.getMessageText())
                .showAlert(false)
                .build();
        telegramClient.execute(answer);

    }

    @Override
    public void answerMessage(Message message) {
        long chatId = message.getChatId();
        long userId = message.getFrom().getId();
        long amount;
        try {
            amount = Long.parseLong(message.getText());
        } catch (NumberFormatException e) {
            sendNumberFormatError(chatId);
            return;
        }
        if (amount < MIN_PAY_AMOUNT || amount > MAX_PAY_AMOUNT) {
            sendSumError(chatId);
            return;
        }

        dialogStageNameRedisStateManager.delete(userId);
        log.info("Delete stage userId={}", userId);
        Optional<PaymentContext> paymentContextOptional = null;
        try {
            paymentContextOptional = paymentContextRedisStateManager.get(userId);
        } catch (Exception e) {
            log.error("REDIS ERROR", e);
            throw new RuntimeException(e);
        }
        PaymentContext paymentContext;
        if (paymentContextOptional.isPresent()) {
            paymentContext = paymentContextOptional.get();
            log.info("Get payment context for userId={}: paymentContext={}", userId, paymentContext);
        } else {
            log.error("Don't get payment context!");
            return;
        }
        PlategaPayment plategaPayment = paymentService.createPlategaPayment(amount, paymentContext.paymentMethod().getIntMethod(), userId);
        log.info("Get plategaPayment={}", plategaPayment);
        String shortId = plategaPayment.getTransactionId().split("-")[0];

        String text = String.format(MessageText.PAYMENT_INSTRUCTION.getMessageText(), amount, shortId);

        List<InlineKeyboardRow> rows = new ArrayList<>();

        InlineKeyboardButton payButton = InlineKeyboardButton.builder()
                .text(ButtonText.PAY_ACTION.getText())
                .url(plategaPayment.getRedirect())
                .build();
        rows.add(new InlineKeyboardRow(payButton));

        String checkPayload = CallbackName.CHECK_STATUS_PAYMENT.getCallbackName()
                              + PAYLOAD_SEPARATOR
                              + plategaPayment.getTransactionId();

        InlineKeyboardButton checkButton = InlineKeyboardButton.builder()
                .text(ButtonText.CHECK_PAYMENT.getText())
                .callbackData(checkPayload)
                .build();
        rows.add(new InlineKeyboardRow(checkButton));

        SendMessage paymentMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(new InlineKeyboardMarkup(rows))
                .build();

        telegramClient.execute(paymentMessage);

    }

    private void sendNumberFormatError (long chatId) {
        SendMessage sendMessage = SendMessage
                .builder()
                .chatId(chatId)
                .text(MessageText.NUMBER_FORMAT_ERROR.getMessageText())
                .parseMode("HTML")
                .build();
        telegramClient.execute(sendMessage);
    }

    private void sendSumError (long chatId) {
        SendMessage sendMessage = SendMessage
                .builder()
                .chatId(chatId)
                .text(MessageText.SUM_PAY_ERROR.getMessageText().formatted(MIN_PAY_AMOUNT, MAX_PAY_AMOUNT))
                .parseMode("HTML")
                .build();
        telegramClient.execute(sendMessage);
    }
}
