package org.example.telegrambot.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegrambot.service.TriviaService;
import org.example.telegrambot.service.UserSessionService;
import org.example.telegrambot.ui.QuizUI;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component("questions")
@RequiredArgsConstructor
@Slf4j
public class QuestionsCommand implements BotCommand {

    private final TriviaService      triviaService;
    private final UserSessionService sessionService;

    @Override
    public String execute(Update update, TelegramClient client) {
        Long chatId = update.getMessage().getChatId();

        TriviaService.QuizState state = triviaService.startQuiz(chatId);

        if (state.questions().isEmpty()) {
            return "❌ No se pudieron cargar las preguntas. Intenta de nuevo en unos segundos.";
        }

        sessionService.recordActivity(chatId);

        try {
            QuizUI.sendQuestion(client, chatId, state, triviaService);
        } catch (Exception e) {
            log.error("Error starting quiz", e);
            return "⚠️ Error iniciando el quiz";
        }
        return "";
    }
}
