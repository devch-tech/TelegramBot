package org.example.telegrambot.commands;

import lombok.RequiredArgsConstructor;
import org.example.telegrambot.service.GitHubIssueService;
import org.example.telegrambot.ui.IssuesUI;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Map;

import static org.example.telegrambot.utils.Constants.USO_COMMAND_ISSUE;

@Component("issue")
@RequiredArgsConstructor
public class IssueCommand implements BotCommand {

    private final GitHubIssueService gitHubIssueService;

    @Override
    public String execute(Update update, TelegramClient client) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText().trim();

        String[] tokens = text.split("\\s+", 3); // /issue lang label
        if (tokens.length < 3) {
            return USO_COMMAND_ISSUE;
        }

        String language = tokens[1];
        String label = tokens[2];

        // primera búsqueda + caché
        List<Map<String, Object>> issues = gitHubIssueService.findAndCache(chatId, language, label);

        try {
            IssuesUI.sendIssuesPage(client, chatId, issues, 1);
        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ Error mostrando las issues";
        }

        // ya enviamos el mensaje con UI → devolvemos vacío para que el handler no duplique
        return "";
    }
}
