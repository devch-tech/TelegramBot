package org.example.telegrambot.ui;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class IssuesUI {
    private static final int PAGE_SIZE = 5;
    private static final String PAGING_PREFIX = "ISSUES_PAGE:";

    public static void sendIssuesPage(TelegramClient client, Long chatId,
                                      List<Map<String, Object>> issues, int page)
            throws TelegramApiException {

        int totalPages = Math.max(1, (int) Math.ceil(issues.size() / (double) PAGE_SIZE));
        page = Math.max(1, Math.min(page, totalPages));

        int from = (page - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, issues.size());
        List<Map<String, Object>> slice = issues.subList(from, to);

        String text = buildMarkdownText(slice, page, totalPages);
        InlineKeyboardMarkup kb = buildKeyboard(slice, page, totalPages);

        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        msg.setParseMode("Markdown"); // o "MarkdownV2" si lo prefieres
        msg.setReplyMarkup(kb);
        client.execute(msg);
    }

    public static void editIssuesPage(TelegramClient client, Long chatId, Integer messageId,
                                      List<Map<String, Object>> issues, int page)
            throws TelegramApiException {

        int totalPages = Math.max(1, (int) Math.ceil(issues.size() / (double) PAGE_SIZE));
        page = Math.max(1, Math.min(page, totalPages));

        int from = (page - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, issues.size());
        List<Map<String, Object>> slice = issues.subList(from, to);

        String text = buildMarkdownText(slice, page, totalPages);
        InlineKeyboardMarkup kb = buildKeyboard(slice, page, totalPages);

        // 👇 En 6.9.x el ctor requiere el texto
        EditMessageText edit = new EditMessageText(text);
        edit.setChatId(String.valueOf(chatId));
        edit.setMessageId(messageId);
        edit.setParseMode("Markdown");
        edit.setReplyMarkup(kb);
        client.execute(edit);
    }

    private static InlineKeyboardMarkup buildKeyboard(List<Map<String, Object>> slice,
                                                      int page, int totalPages) {
        // 👇 Ahora se usa InlineKeyboardRow
        List<InlineKeyboardRow> rows = new ArrayList<>();

        for (Map<String, Object> issue : slice) {
            String url = String.valueOf(issue.get("html_url"));
            String title = safe(String.valueOf(issue.get("title")), 28);

            InlineKeyboardButton open = new InlineKeyboardButton("🔗 " + title);
            open.setUrl(url);

            InlineKeyboardRow row = new InlineKeyboardRow();
            row.add(open);
            rows.add(row);
        }

        InlineKeyboardButton prev = new InlineKeyboardButton("◀️");
        prev.setCallbackData(PAGING_PREFIX + Math.max(1, page - 1));

        InlineKeyboardButton refresh = new InlineKeyboardButton("🔄");
        refresh.setCallbackData("ISSUES_REFRESH");

        InlineKeyboardButton next = new InlineKeyboardButton("▶️");
        next.setCallbackData(PAGING_PREFIX + Math.min(totalPages, page + 1));

        InlineKeyboardRow nav = new InlineKeyboardRow();
        nav.add(prev);
        nav.add(refresh);
        nav.add(next);
        rows.add(nav);

        InlineKeyboardMarkup kb = new InlineKeyboardMarkup(rows);
        kb.setKeyboard(rows);
        return kb;
    }

    // --- helpers (sin cambios) ---
    private static String buildMarkdownText(List<Map<String, Object>> slice, int page, int totalPages) {
        String header = "🚀 *Issues encontradas*  _pág. " + page + "/" + totalPages + "_\n\n";
        StringBuilder body = new StringBuilder();
        int idx = 1;
        for (Map<String, Object> issue : slice) {
            String title = String.valueOf(issue.get("title"));
            String repo = extractRepo(issue);
            body.append("🔹 *").append(idx).append(".* ").append(safe(title, 70))
                    .append(" — _").append(repo).append("_\n");
            idx++;
        }
        return header + body + "\n";
    }

    private static String safe(String s, int max) {
        if (s == null) return "";
        s = s.replace("\n", " ").trim();
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    private static String extractRepo(Map<String, Object> issue) {
        String url = String.valueOf(issue.get("html_url"));
        try {
            String[] parts = url.split("/");
            // ...github.com/{owner}/{repo}/issues/{id}
            int i = Arrays.asList(parts).indexOf("github.com");
            if (i >= 0 && i + 2 < parts.length) return parts[i + 1] + "/" + parts[i + 2];
        } catch (Exception ignored) {}
        return "repo";
    }
}
