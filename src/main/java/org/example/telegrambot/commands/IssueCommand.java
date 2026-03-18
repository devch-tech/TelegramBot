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

    /** Maps user-friendly complexity levels to GitHub labels. */
    private static final Map<String, String> COMPLEXITY_MAP = Map.of(
            "principiante", "good first issue",
            "beginner",     "good first issue",
            "intermedio",   "help wanted",
            "intermediate", "help wanted",
            "avanzado",     "bug",
            "advanced",     "bug"
    );

    @Override
    public String execute(Update update, TelegramClient client) {
        Long   chatId = update.getMessage().getChatId();
        String text   = update.getMessage().getText().trim();
        String[] tokens = text.split("\\s+", 3);

        // /issue  →  use saved language + show level selector
        if (tokens.length < 2) {
            return sendLevelSelector(client, chatId, sessionService.getLanguageOrDefault(chatId));
        }

        String language = tokens[1].toLowerCase();
        sessionService.setLanguage(chatId, language);

        // /issue java  →  show level selector
        if (tokens.length < 3) {
            return sendLevelSelector(client, chatId, language);
        }

        // /issue java principiante  (or raw GitHub label)
        String rawLevel = tokens[2].toLowerCase();
        String label    = COMPLEXITY_MAP.getOrDefault(rawLevel, rawLevel);

        List<Map<String, Object>> issues =
                gitHubIssueService.findAndCache(chatId, language, label);

        sessionService.addIssueExplored(chatId);

        try {
            IssuesUI.sendIssuesPage(client, chatId, issues, 1);
        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ Error mostrando las issues";
        }
        return "";
    }

    /** Sends an inline keyboard so the user can pick difficulty level. */
    public static String sendLevelSelector(TelegramClient client, Long chatId, String language) {
        String langCap = Character.toUpperCase(language.charAt(0)) + language.substring(1);

        String[][] levels = {
                {"🟢 Principiante", "ISSUE_LEVEL:" + language + ":principiante"},
                {"🟡 Intermedio",   "ISSUE_LEVEL:" + language + ":intermedio"},
                {"🔴 Avanzado",     "ISSUE_LEVEL:" + language + ":avanzado"}
        };

        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (String[] level : levels) {
            InlineKeyboardButton btn = new InlineKeyboardButton(level[0]);
            btn.setCallbackData(level[1]);
            InlineKeyboardRow row = new InlineKeyboardRow();
            row.add(btn);
            rows.add(row);
        }

        InlineKeyboardMarkup kb = new InlineKeyboardMarkup(rows);
        kb.setKeyboard(rows);

        SendMessage msg = new SendMessage(String.valueOf(chatId),
                "🔍 *Issues de " + langCap + "* — Elige nivel de dificultad:");
        msg.setParseMode("Markdown");
        msg.setReplyMarkup(kb);

        try { client.execute(msg); } catch (Exception e) { e.printStackTrace(); }
        return "";
    }
}
