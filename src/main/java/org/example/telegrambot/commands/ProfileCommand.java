package org.example.telegrambot.commands;

import lombok.RequiredArgsConstructor;
import org.example.telegrambot.service.UserSessionService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component("profile")
@RequiredArgsConstructor
public class ProfileCommand implements BotCommand {

    private final UserSessionService sessionService;

    @Override
    public String execute(Update update, TelegramClient client) {
        Long   chatId    = update.getMessage().getChatId();
        String firstName = update.getMessage().getFrom().getFirstName();

        String  language  = sessionService.getLanguage(chatId);
        int     streak    = sessionService.getStreak(chatId);
        int     exercises = sessionService.getExercisesDone(chatId);
        int     issues    = sessionService.getIssuesExplored(chatId);
        int     quizzes   = sessionService.getQuizzesCompleted(chatId);
        int     score     = sessionService.getTotalQuizScore(chatId);

        String streakIcon = streak >= 30 ? "🏆"
                          : streak >= 7  ? "🔥"
                          : streak >= 3  ? "⚡"
                          : "🌱";

        String langDisplay = language != null
                ? Character.toUpperCase(language.charAt(0)) + language.substring(1)
                : "No configurado — usa /start";

        return "*👤 Perfil de " + firstName + "*\n" +
               "━━━━━━━━━━━━━━━━━━\n" +
               streakIcon + " Racha: *" + streak + " día" + (streak != 1 ? "s" : "") + "*\n" +
               "💻 Lenguaje: *" + langDisplay + "*\n" +
               "━━━━━━━━━━━━━━━━━━\n" +
               "📊 *Estadísticas*\n" +
               "💪 Ejercicios explorados: *" + exercises + "*\n" +
               "🔍 Issues revisadas: *" + issues + "*\n" +
               "🧠 Quizzes completados: *" + quizzes + "*\n" +
               "⭐ Puntos en quiz: *" + score + "*\n" +
               "━━━━━━━━━━━━━━━━━━\n" +
               "_Usa /daily para mantener tu racha_\n" +
               "_Usa /start para cambiar tu lenguaje_";
    }
}
