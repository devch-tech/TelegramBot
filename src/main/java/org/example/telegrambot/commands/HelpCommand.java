package org.example.telegrambot.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.example.telegrambot.utils.Constants.HELP_MESSAGE;

@Component("help")
@RequiredArgsConstructor
public class HelpCommand implements BotCommand{
    @Override
    public String execute(Update update, TelegramClient client) {
        return HELP_MESSAGE;
    }
}
