package tg.configpaymentservice.messaging.consumers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import tg.configpaymentservice.constants.DialogStageName;
import tg.zhenshen_bot_utils.containers.CallbackContainer;
import tg.zhenshen_bot_utils.containers.DialogStateContainer;
import tg.zhenshen_bot_utils.state.manager.RedisStateManager;

import java.util.Optional;
import java.util.function.Consumer;


@Slf4j
@Service("telegramCallbackQueryConsumer")
@RequiredArgsConstructor
public class TelegramCallbackQueryConsumer implements Consumer<CallbackQuery> {

    private final CallbackContainer callbackContainer;
    private final DialogStateContainer dialogStateContainer;
    private final RedisStateManager<Long, DialogStageName> dialogStageNameRedisStateManager;

    @Override
    public void accept(CallbackQuery callbackQuery) {
        log.info("Received CallbackQuery: userId={}", callbackQuery.getFrom().getId());
        Long userCallbackId = callbackQuery.getFrom().getId();
        Optional<DialogStageName> stage = dialogStageNameRedisStateManager.get(userCallbackId);
        if (stage.isPresent()) {
            dialogStateContainer.retrieveDialogStage(stage.get().getDialogStageName()).processCallbackQuery(callbackQuery);
            return;
        }
        String callbackIdentifier = callbackQuery.getData().split(":")[0];
        try {
            callbackContainer.retrieveCallback(callbackIdentifier).processCallback(callbackQuery);
        } catch (NullPointerException e) {
            log.warn("Callback with id={} didn't found", callbackIdentifier);
        }
    }
}
