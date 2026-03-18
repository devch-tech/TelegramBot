package org.example.telegrambot.commands;

import lombok.RequiredArgsConstructor;
import org.example.telegrambot.service.GitHubIssueService;
import org.example.telegrambot.service.UserSessionService;
import org.example.telegrambot.ui.IssuesUI;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("issue")
@RequiredArgsConstructor
public class IssueCommand implements BotCommand {

    private final GitHubIssueService gitHubIssueService;
    private final UserSessionService sessionService;

    @Override
    public String execute(Update update, TelegramClient client) {
        Long   chatId   = update.getMessage().getChatId();
        String language = sessionService.getLanguageOrDefault(chatId);
        sendHumanLanguageSelector(client, chatId, language);
        return "";
    }

    // ── Step 1: choose human language ────────────────────────────────────────

    public static void sendHumanLanguageSelector(TelegramClient client, Long chatId, String progLang) {
        String langCap = capitalize(progLang);

        InlineKeyboardButton esBtn = new InlineKeyboardButton("🇪🇸 Español");
        esBtn.setCallbackData("ISSUE_HLANG:" + progLang + ":es");

        InlineKeyboardButton enBtn = new InlineKeyboardButton("🇬🇧 English");
        enBtn.setCallbackData("ISSUE_HLANG:" + progLang + ":en");

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(esBtn);
        row.add(enBtn);

        InlineKeyboardMarkup kb = new InlineKeyboardMarkup(List.of(row));
        kb.setKeyboard(List.of(row));

        SendMessage msg = new SendMessage(String.valueOf(chatId),
                "🔍 *Issues de " + langCap + "*\n¿En qué idioma quieres las issues?");
        msg.setParseMode("Markdown");
        msg.setReplyMarkup(kb);

        try { client.execute(msg); } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Step 2: choose label ─────────────────────────────────────────────────

    public static void sendLabelSelector(TelegramClient client, Long chatId,
                                          String progLang, String humanLang) {
        String langCap    = capitalize(progLang);
        String humanLabel = "es".equals(humanLang) ? "🇪🇸 Español" : "🇬🇧 English";

        String prefix = "ISSUE_LABEL:" + progLang + ":" + humanLang + ":";

        String[][] labels = {
                {"🟢 Good First Issue", prefix + "gfi"},
                {"🤝 Help Wanted",      prefix + "hw"},
                {"🐛 Bug",              prefix + "bug"},
                {"📋 Todas",            prefix + "all"}
        };

        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (String[] label : labels) {
            InlineKeyboardButton btn = new InlineKeyboardButton(label[0]);
            btn.setCallbackData(label[1]);
            InlineKeyboardRow row = new InlineKeyboardRow();
            row.add(btn);
            rows.add(row);
        }

        InlineKeyboardMarkup kb = new InlineKeyboardMarkup(rows);
        kb.setKeyboard(rows);

        SendMessage msg = new SendMessage(String.valueOf(chatId),
                "🔍 *Issues de " + langCap + "* (" + humanLabel + ")\n¿Qué tipo de issues buscas?");
        msg.setParseMode("Markdown");
        msg.setReplyMarkup(kb);

        try { client.execute(msg); } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Step 3: search & display ─────────────────────────────────────────────

    public static void searchAndSend(TelegramClient client, Long chatId,
                                      String progLang, String humanLang, String labelCode,
                                      GitHubIssueService gitHubIssueService,
                                      UserSessionService sessionService) {
        List<Map<String, Object>> issues =
                gitHubIssueService.findAndCache(chatId, progLang, labelCode, humanLang);
        sessionService.addIssueExplored(chatId);
        try {
            IssuesUI.sendIssuesPage(client, chatId, issues, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
