package org.example.telegrambot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegrambot.commands.BotCommand;
import org.example.telegrambot.commands.IssueCommand;
import org.example.telegrambot.commands.StartCommand;
import org.example.telegrambot.service.ExercismService;
import org.example.telegrambot.service.GitHubIssueService;
import org.example.telegrambot.service.TriviaService;
import org.example.telegrambot.service.UserSessionService;
import org.example.telegrambot.ui.ExerciseUI;
import org.example.telegrambot.ui.IssuesUI;
import org.example.telegrambot.ui.QuizUI;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Map;

import static org.example.telegrambot.utils.Constants.DEFAULT_COMMANDS;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramCommandHandler {

    private final ApplicationContext  context;
    private final GitHubIssueService  gitHubIssueService;
    private final TriviaService       triviaService;
    private final UserSessionService  sessionService;
    private final ExercismService     exercismService;

    public void handleUpdate(Update update, TelegramClient client) {
        try {
            // ── 1) Callbacks ──────────────────────────────────────────────────
            if (update.hasCallbackQuery()) {
                var     cq        = update.getCallbackQuery();
                String  data      = cq.getData();
                Long    chatId    = cq.getMessage().getChatId();
                Integer messageId = cq.getMessage().getMessageId();

                if (data == null) return;

                // Issues pagination
                if (data.startsWith("ISSUES_PAGE:")) {
                    int page = safeParseInt(data.substring("ISSUES_PAGE:".length()), 1);
                    List<Map<String, Object>> issues = gitHubIssueService.getLastIssues(chatId);
                    if (issues != null && !issues.isEmpty())
                        IssuesUI.editIssuesPage(client, chatId, messageId, issues, page);
                    return;
                }

                if (data.equals("ISSUES_REFRESH")) {
                    List<Map<String, Object>> issues = gitHubIssueService.refresh(chatId);
                    IssuesUI.editIssuesPage(client, chatId, messageId, issues, 1);
                    return;
                }

                // User chose label type → ISSUE_LABEL:java:gfi
                if (data.startsWith("ISSUE_LABEL:")) {
                    String[] parts = data.substring("ISSUE_LABEL:".length()).split(":", 2);
                    if (parts.length == 2) {
                        String progLang  = parts[0];
                        String labelCode = parts[1];
                        IssueCommand.searchAndSend(client, chatId, progLang, labelCode,
                                gitHubIssueService, sessionService);
                    }
                    return;
                }

                // First language chosen from /start → LANG:java
                if (data.startsWith("LANG:") && !data.startsWith("LANG2:")) {
                    String language = data.substring("LANG:".length());
                    sessionService.setFirstLanguage(chatId, language);
                    String langCap = capitalize(language);

                    SendMessage ask = new SendMessage(String.valueOf(chatId),
                            "✅ *" + langCap + "* guardado como lenguaje principal.\n\n" +
                            "¿Quieres añadir un *segundo lenguaje* favorito?");
                    ask.setParseMode("Markdown");
                    ask.setReplyMarkup(StartCommand.buildKeyboard("LANG2", language));
                    client.execute(ask);
                    return;
                }

                // Second language chosen → LANG2:python
                if (data.startsWith("LANG2:")) {
                    String language = data.substring("LANG2:".length());
                    sessionService.addSecondLanguage(chatId, language);
                    List<String> langs = sessionService.getLanguages(chatId);
                    String lang1 = capitalize(langs.get(0));
                    String lang2 = langs.size() > 1 ? capitalize(langs.get(1)) : "";
                    String confirm = "🎉 ¡Perfecto! Tus lenguajes favoritos son:\n" +
                            "1️⃣ *" + lang1 + "*\n" +
                            "2️⃣ *" + lang2 + "*\n\n" +
                            buildCommandsMenu();
                    SendMessage msg = new SendMessage(String.valueOf(chatId), confirm);
                    msg.setParseMode("Markdown");
                    client.execute(msg);
                    return;
                }

                // Skipped second language → LANG_SKIP
                if (data.equals("LANG_SKIP")) {
                    String language = sessionService.getLanguageOrDefault(chatId);
                    String confirm = "✅ ¡Listo! Tu lenguaje principal es *" + capitalize(language) + "*\n\n" +
                            buildCommandsMenu();
                    SendMessage msg = new SendMessage(String.valueOf(chatId), confirm);
                    msg.setParseMode("Markdown");
                    client.execute(msg);
                    return;
                }

                // Quiz answer → QA:0 / QA:1 / QA:2 / QA:3
                if (data.startsWith("QA:")) {
                    int answerIdx = safeParseInt(data.substring("QA:".length()), -1);
                    if (answerIdx >= 0) handleQuizAnswer(chatId, answerIdx, client);
                    return;
                }

                // Exercises pagination → EX_PAGE:java:2
                if (data.startsWith("EX_PAGE:")) {
                    String[] parts = data.substring("EX_PAGE:".length()).split(":", 2);
                    if (parts.length == 2) {
                        String language = parts[0];
                        int    page     = safeParseInt(parts[1], 1);
                        List<Map<String, Object>> exercises = exercismService.getLastExercises(chatId);
                        if (!exercises.isEmpty())
                            ExerciseUI.sendExercisesPage(client, chatId, exercises, page, language);
                    }
                    return;
                }

                // Exercises refresh → EX_REFRESH:java
                if (data.startsWith("EX_REFRESH:")) {
                    String language = data.substring("EX_REFRESH:".length());
                    List<Map<String, Object>> exercises =
                            exercismService.fetchAndCache(chatId, language, null);
                    ExerciseUI.sendExercisesPage(client, chatId, exercises, 1, language);
                    return;
                }

                return;
            }

            // ── 2) Text messages ──────────────────────────────────────────────
            if (!update.hasMessage() || !update.getMessage().hasText()) return;

            String chatIdStr   = update.getMessage().getChatId().toString();
            String text        = update.getMessage().getText().trim();
            String commandName = text.split("\\s+")[0].toLowerCase();

            if (commandName.startsWith("/")) commandName = commandName.substring(1);
            int atIdx = commandName.indexOf('@');
            if (atIdx > 0) commandName = commandName.substring(0, atIdx);

            BotCommand command = null;
            try {
                command = (BotCommand) context.getBean(commandName);
            } catch (Exception ignored) { /* no bean → default reply */ }

            String reply = (command != null) ? command.execute(update, client) : DEFAULT_COMMANDS;

            if (reply != null && !reply.isBlank()) {
                SendMessage msg = new SendMessage(chatIdStr, reply);
                msg.setParseMode("Markdown");
                client.execute(msg);
            }

        } catch (TelegramApiException e) {
            log.error("TelegramApiException", e);
        } catch (Exception e) {
            log.error("Unexpected error", e);
        }
    }

    // ── Quiz answer logic ─────────────────────────────────────────────────────

    private void handleQuizAnswer(Long chatId, int answerIdx, TelegramClient client) throws Exception {
        TriviaService.QuizState state = triviaService.getQuizState(chatId);
        if (state == null || state.isFinished()) return;

        TriviaService.QuizQuestion currentQ = state.questions().get(state.currentIdx());
        boolean correct = (answerIdx == currentQ.correctIndex());

        String feedback = correct
                ? "✅ *¡Correcto!*"
                : "❌ *Incorrecto.*\nLa respuesta correcta era: _" +
                  currentQ.answers().get(currentQ.correctIndex())
                          .replace("*", "\\*").replace("_", "\\_") + "_";

        SendMessage feedbackMsg = new SendMessage(String.valueOf(chatId), feedback);
        feedbackMsg.setParseMode("Markdown");
        client.execute(feedbackMsg);

        TriviaService.QuizState next = state.advance(correct);
        triviaService.updateQuizState(chatId, next);

        if (next.isFinished()) {
            triviaService.endQuiz(chatId);
            sessionService.addQuizCompleted(chatId, next.score());
            SendMessage result = new SendMessage(String.valueOf(chatId),
                    QuizUI.buildFinalResult(next.score(), next.questions().size()));
            result.setParseMode("Markdown");
            client.execute(result);
        } else {
            QuizUI.sendQuestion(client, chatId, next, triviaService);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String buildCommandsMenu() {
        return "Comandos disponibles:\n" +
               "🔍 /issue — Issues de GitHub\n" +
               "💪 /exercises — Ejercicios de Exercism\n" +
               "🧠 /questions — Quiz de 20 preguntas\n" +
               "📅 /daily — Reto diario\n" +
               "📊 /profile — Tu progreso\n" +
               "❓ /help — Ayuda completa";
    }

    private int safeParseInt(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return fallback; }
    }
}
