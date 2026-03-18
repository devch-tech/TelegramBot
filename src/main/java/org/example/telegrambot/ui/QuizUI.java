package org.example.telegrambot.ui;

import org.example.telegrambot.service.TriviaService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class QuizUI {

    private static final String[] LABELS = {"A", "B", "C", "D"};

    /**
     * Sends the current question from state, stores the returned messageId back in TriviaService.
     */
    public static void sendQuestion(TelegramClient client, Long chatId,
                                    TriviaService.QuizState state,
                                    TriviaService triviaService) throws TelegramApiException {

        if (state.questions().isEmpty() || state.isFinished()) return;

        TriviaService.QuizQuestion q = state.questions().get(state.currentIdx());
        int questionNum = state.currentIdx() + 1;
        int total       = state.questions().size();

        String text = "🧠 *Pregunta " + questionNum + "/" + total + "*\n" +
                buildProgressBar(questionNum, total) + "\n\n" +
                escape(q.question()) + "\n\n" +
                "_Selecciona tu respuesta:_";

        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        msg.setParseMode("Markdown");
        msg.setReplyMarkup(buildAnswerKeyboard(q));

        Message sent = client.execute(msg);
        triviaService.updateQuizState(chatId, state.withMessageId((long) sent.getMessageId()));
    }

    /** Builds the final score message after 20 questions. */
    public static String buildFinalResult(int score, int total) {
        int pct = total > 0 ? (score * 100 / total) : 0;
        String medal = pct >= 80 ? "🏆" : pct >= 60 ? "🥈" : pct >= 40 ? "🥉" : "📚";

        return medal + " *Quiz completado!*\n\n" +
                "✅ Correctas: *" + score + " / " + total + "*\n" +
                "📊 Puntuación: *" + pct + "%*\n\n" +
                getComment(pct);
    }

    // --- private helpers ---

    private static InlineKeyboardMarkup buildAnswerKeyboard(TriviaService.QuizQuestion q) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (int i = 0; i < q.answers().size(); i++) {
            String label   = LABELS[i] + ") " + safe(q.answers().get(i), 45);
            InlineKeyboardButton btn = new InlineKeyboardButton(label);
            btn.setCallbackData("QA:" + i);
            InlineKeyboardRow row = new InlineKeyboardRow();
            row.add(btn);
            rows.add(row);
        }
        InlineKeyboardMarkup kb = new InlineKeyboardMarkup(rows);
        kb.setKeyboard(rows);
        return kb;
    }

    private static String buildProgressBar(int current, int total) {
        int filled = total > 0 ? (current * 10 / total) : 0;
        StringBuilder bar = new StringBuilder("`[");
        for (int i = 0; i < 10; i++) bar.append(i < filled ? "█" : "░");
        bar.append("]`");
        return bar.toString();
    }

    private static String getComment(int pct) {
        if (pct == 100) return "_¡Perfecto! Eres un crack 🔥_";
        if (pct >= 80)  return "_¡Excelente resultado! Sigue así 💪_";
        if (pct >= 60)  return "_Buen trabajo. ¡Puedes mejorar más!_";
        if (pct >= 40)  return "_Sigue practicando, vas por buen camino 📖_";
        return "_No te rindas, la próxima vez mejor 💡_";
    }

    private static String safe(String s, int max) {
        if (s == null) return "";
        s = s.replace("\n", " ").trim();
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    /** Minimal Markdown escaping for question text (avoid breaking bold/italic). */
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("*", "\\*").replace("_", "\\_").replace("`", "\\`");
    }
}
