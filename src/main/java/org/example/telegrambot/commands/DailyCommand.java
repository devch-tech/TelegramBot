package org.example.telegrambot.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegrambot.service.ExercismService;
import org.example.telegrambot.service.GitHubIssueService;
import org.example.telegrambot.service.TriviaService;
import org.example.telegrambot.service.UserSessionService;
import org.example.telegrambot.ui.ExerciseUI;
import org.example.telegrambot.ui.IssuesUI;
import org.example.telegrambot.ui.QuizUI;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Map;

@Component("daily")
@RequiredArgsConstructor
@Slf4j
public class DailyCommand implements BotCommand {

    private final UserSessionService sessionService;
    private final GitHubIssueService gitHubIssueService;
    private final ExercismService    exercismService;
    private final TriviaService      triviaService;

    @Override
    public String execute(Update update, TelegramClient client) {
        Long chatId = update.getMessage().getChatId();

        if (sessionService.hasDoneDaily(chatId)) {
            int streak = sessionService.getStreak(chatId);
            return "✅ Ya completaste tu reto diario de hoy.\n" +
                   "🔥 Racha actual: *" + streak + " día" + (streak != 1 ? "s" : "") + "*\n\n" +
                   "_¡Vuelve mañana para seguir la racha!_";
        }

        String language = sessionService.getLanguageOrDefault(chatId);
        String langCap  = Character.toUpperCase(language.charAt(0)) + language.substring(1);

        try {
            // ── Header ────────────────────────────────────────────────────────
            sendMd(client, chatId,
                    "📅 *Reto Diario — " + langCap + "*\n\n" +
                    "¡Tu dosis diaria de práctica! Completa los 3 retos para mantener tu racha 🔥");

            // ── 1. Issue ──────────────────────────────────────────────────────
            sendMd(client, chatId, "🔍 *1 · Issue del día*");
            List<Map<String, Object>> issues =
                    gitHubIssueService.findAndCache(chatId, language, "good first issue");
            if (!issues.isEmpty()) {
                IssuesUI.sendIssuesPage(client, chatId, issues.subList(0, Math.min(3, issues.size())), 1);
            } else {
                sendMd(client, chatId, "❌ No encontré issues hoy para " + langCap + ".");
            }

            // ── 2. Exercise ───────────────────────────────────────────────────
            sendMd(client, chatId, "💪 *2 · Ejercicio del día*");
            List<Map<String, Object>> exercises =
                    exercismService.fetchAndCache(chatId, language, "easy");
            if (!exercises.isEmpty()) {
                ExerciseUI.sendExercisesPage(client, chatId,
                        exercises.subList(0, Math.min(1, exercises.size())), 1, language);
            } else {
                sendMd(client, chatId, "❌ No encontré ejercicios hoy para " + langCap + ".");
            }

            // ── 3. Quiz question ──────────────────────────────────────────────
            sendMd(client, chatId,
                    "🧠 *3 · Pregunta del día*\nCaliente la mente con una pregunta de programación:");
            List<TriviaService.QuizQuestion> questions = triviaService.fetchQuestions(1);
            if (!questions.isEmpty()) {
                TriviaService.QuizState state =
                        new TriviaService.QuizState(questions, 0, 0, null);
                triviaService.updateQuizState(chatId, state);
                QuizUI.sendQuestion(client, chatId, state, triviaService);
            }

            // ── Footer ────────────────────────────────────────────────────────
            sessionService.markDailyDone(chatId);
            int streak = sessionService.getStreak(chatId);
            sendMd(client, chatId,
                    "🔥 *¡Racha actual: " + streak + " día" + (streak != 1 ? "s" : "") + "!*\n\n" +
                    "_Vuelve mañana para seguir tu racha. ¡Tú puedes!_");

        } catch (Exception e) {
            log.error("Error generating daily challenge", e);
            return "⚠️ Error generando el reto diario";
        }

        return "";
    }

    private void sendMd(TelegramClient client, Long chatId, String text) throws Exception {
        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        msg.setParseMode("Markdown");
        client.execute(msg);
    }
}
