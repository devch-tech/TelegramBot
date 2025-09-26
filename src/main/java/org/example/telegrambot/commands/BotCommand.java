package org.example.telegrambot.commands;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public interface BotCommand {
    /**
     * Ejecuta la lógica del comando y devuelve la respuesta a enviar.
     */
    String execute(Update update, TelegramClient client);
}
