package org.example.telegrambot.commands;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component("/exercises")
public class ExercisesCommand implements BotCommand{
    @Override
    public String execute(Update update, TelegramClient client) {
        return "";
    }
}
