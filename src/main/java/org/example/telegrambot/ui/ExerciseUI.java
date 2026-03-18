package org.example.telegrambot.ui;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExerciseUI {

    private static final int PAGE_SIZE = 5;

    public static void sendExercisesPage(TelegramClient client, Long chatId,
                                         List<Map<String, Object>> exercises,
                                         int page, String language) throws TelegramApiException {

        int totalPages = Math.max(1, (int) Math.ceil(exercises.size() / (double) PAGE_SIZE));
        page = Math.max(1, Math.min(page, totalPages));

        int from  = (page - 1) * PAGE_SIZE;
        int to    = Math.min(from + PAGE_SIZE, exercises.size());
        List<Map<String, Object>> slice = exercises.subList(from, to);

        String text = buildText(slice, page, totalPages, language);
        InlineKeyboardMarkup kb = buildKeyboard(slice, page, totalPages, language);

        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        msg.setParseMode("Markdown");
        msg.setReplyMarkup(kb);
        client.execute(msg);
    }

    // --- private helpers ---

    private static String buildText(List<Map<String, Object>> slice,
                                    int page, int totalPages, String language) {
        StringBuilder sb = new StringBuilder(
                "💪 *Ejercicios de " + capitalize(language) +
                "* — _pág. " + page + "/" + totalPages + "_\n\n");

        int idx = 1;
        for (Map<String, Object> ex : slice) {
            String title      = safe(String.valueOf(ex.get("title")), 60);
            String difficulty = String.valueOf(ex.getOrDefault("difficulty", "medium"));
            String blurb      = String.valueOf(ex.getOrDefault("blurb", ""));

            String icon = switch (difficulty.toLowerCase()) {
                case "easy"   -> "🟢";
                case "medium" -> "🟡";
                case "hard"   -> "🔴";
                default       -> "⚪";
            };

            sb.append(icon).append(" *").append(idx).append(".* ").append(title).append("\n");
            if (!blurb.isBlank() && !"null".equals(blurb)) {
                sb.append("_").append(safe(blurb, 90)).append("_\n");
            }
            sb.append("\n");
            idx++;
        }
        return sb.toString();
    }

    private static InlineKeyboardMarkup buildKeyboard(List<Map<String, Object>> slice,
                                                       int page, int totalPages, String language) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        String track = language.toLowerCase();

        for (Map<String, Object> ex : slice) {
            String slug  = String.valueOf(ex.get("slug"));
            String title = safe(String.valueOf(ex.get("title")), 28);
            String url   = "https://exercism.org/tracks/" + track + "/exercises/" + slug;

            InlineKeyboardButton btn = new InlineKeyboardButton("🔗 " + title);
            btn.setUrl(url);
            InlineKeyboardRow row = new InlineKeyboardRow();
            row.add(btn);
            rows.add(row);
        }

        // Navigation row
        InlineKeyboardButton prev    = new InlineKeyboardButton("◀️");
        InlineKeyboardButton refresh = new InlineKeyboardButton("🔄");
        InlineKeyboardButton next    = new InlineKeyboardButton("▶️");
        prev.setCallbackData("EX_PAGE:" + language + ":" + Math.max(1, page - 1));
        refresh.setCallbackData("EX_REFRESH:" + language);
        next.setCallbackData("EX_PAGE:" + language + ":" + Math.min(totalPages, page + 1));

        InlineKeyboardRow nav = new InlineKeyboardRow();
        nav.add(prev); nav.add(refresh); nav.add(next);
        rows.add(nav);

        InlineKeyboardMarkup kb = new InlineKeyboardMarkup(rows);
        kb.setKeyboard(rows);
        return kb;
    }

    private static String safe(String s, int max) {
        if (s == null || "null".equals(s)) return "";
        s = s.replace("\n", " ").trim();
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
