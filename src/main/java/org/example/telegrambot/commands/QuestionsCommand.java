package org.example.telegrambot.commands;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component("/questions")
public class QuestionsCommand implements BotCommand {

    @Override
    public String execute(Update update, TelegramClient client) {
        return "";
    }
}
