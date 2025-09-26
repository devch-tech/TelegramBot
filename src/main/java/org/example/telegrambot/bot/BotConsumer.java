// src/main/java/org/example/telegrambot/bot/BotConsumer.java
package org.example.telegrambot.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class BotConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramCommandHandler handler;
    private final TelegramClient client;

    @Override
    public void consume(Update update) {
        handler.handleUpdate(update, client);
    }
}
