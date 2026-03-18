package org.example.telegrambot.commands;

import lombok.RequiredArgsConstructor;
import org.example.telegrambot.service.ExercismService;
import org.example.telegrambot.service.UserSessionService;
import org.example.telegrambot.ui.ExerciseUI;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Map;

@Component("exercises")
@RequiredArgsConstructor
public class ExercisesCommand implements BotCommand {

    private final ExercismService    exercismService;
    private final UserSessionService sessionService;

    @Override
    public String execute(Update update, TelegramClient client) {
        Long   chatId = update.getMessage().getChatId();
        String text   = update.getMessage().getText().trim();
        String[] tokens = text.split("\\s+");

        String language;
        String difficulty = null;

        if (tokens.length >= 2) {
            language = tokens[1].toLowerCase();
            if (tokens.length >= 3) difficulty = tokens[2];
        } else if (sessionService.hasLanguage(chatId)) {
            language = sessionService.getLanguage(chatId);
        } else {
            return "💬 Dime el lenguaje: `/exercises java` o `/exercises java principiante`\n" +
                   "O usa /start para guardar tu lenguaje favorito.";
        }

        sessionService.setLanguage(chatId, language);

        List<Map<String, Object>> exercises =
                exercismService.fetchAndCache(chatId, language, difficulty);

        if (exercises.isEmpty()) {
            return "❌ No encontré ejercicios para *" + language + "*.\n" +
                   "Verifica que el lenguaje sea válido (ej: java, python, javascript).";
        }

        sessionService.addExerciseDone(chatId);

        try {
            ExerciseUI.sendExercisesPage(client, chatId, exercises, 1, language);
        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ Error mostrando los ejercicios";
        }
        return "";
    }
}
