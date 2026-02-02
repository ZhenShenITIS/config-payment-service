package tg.configpaymentservice.messaging.consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import org.telegram.telegrambots.meta.api.objects.message.Message;
import tg.configpaymentservice.constants.DialogStageName;
import tg.zhenshen_bot_utils.containers.CommandContainer;
import tg.zhenshen_bot_utils.containers.DialogStateContainer;
import tg.zhenshen_bot_utils.state.manager.RedisStateManager;


import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramMessageConsumer implements Consumer<Message> {
    private final CommandContainer commandContainer;
    private final DialogStateContainer dialogStateContainer;
    private final RedisStateManager<Long, DialogStageName> dialogStageNameRedisStateManager;

    @Override
    public void accept(Message message) {
        log.info("Received message with text \"{}\" from userId={}", message.getText(), message.getFrom().getId());

        Optional<DialogStageName> stage = dialogStageNameRedisStateManager.get(message.getFrom().getId());
        log.info("stage: {}", stage);
        if (stage.isPresent()) {
            dialogStateContainer.retrieveDialogStage(stage.get().getDialogStageName())
                    .answerMessage(message);
        } else {
            boolean hasText = message.hasText();
            boolean hasCaption = message.hasCaption();

            if (hasText || hasCaption) {
                String text = hasText ? message.getText() : message.getCaption();

                if (text.startsWith("/")) {
                    String commandIdentifier = text.split(" ")[0]
                            .split("\n")[0]
                            .toLowerCase();

                    try {
                        commandContainer.retrieveCommand(commandIdentifier)
                                .handleCommand(message);
                    } catch (NullPointerException e) {
                        log.warn("Command with id={} didn't found", commandIdentifier);
                    }
                }
            }
        }
    }
}
