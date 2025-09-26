package org.example.telegrambot.bot;

import lombok.RequiredArgsConstructor;
import org.example.telegrambot.commands.BotCommand;
import org.example.telegrambot.service.GitHubIssueService;
import org.example.telegrambot.ui.IssuesUI;
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
public class TelegramCommandHandler {

    private final ApplicationContext context;          // Para buscar beans de comandos
    private final GitHubIssueService gitHubIssueService; // Para callbacks de issues (refresh / paginación)

    public void handleUpdate(Update update, TelegramClient client) {
        try {
            // --- 1) Callbacks (inline keyboard) ---
            if (update.hasCallbackQuery()) {
                var cq = update.getCallbackQuery();
                String data = cq.getData();
                Long chatId = cq.getMessage().getChatId();
                Integer messageId = cq.getMessage().getMessageId();

                if (data != null) {
                    if (data.startsWith("ISSUES_PAGE:")) {
                        // Paginar usando los últimos resultados cacheados
                        int page = safeParseInt(data.substring("ISSUES_PAGE:".length()), 1);
                        List<Map<String, Object>> issues = gitHubIssueService.getLastIssues(chatId);
                        if (issues != null && !issues.isEmpty()) {
                            IssuesUI.editIssuesPage(client, chatId, messageId, issues, page);
                        }
                        return;
                    }
                    if (data.equals("ISSUES_REFRESH")) {
                        // Repetir consulta con el mismo contexto (lang/label/fecha)
                        List<Map<String, Object>> issues = gitHubIssueService.refresh(chatId);
                        IssuesUI.editIssuesPage(client, chatId, messageId, issues, 1);
                        return;
                    }
                }
                // Si el callback no es para issues, simplemente ignora o añade otros casos aquí.
                return;
            }

            // --- 2) Mensajes de texto ---
            if (!update.hasMessage() || !update.getMessage().hasText()) return;

            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText().trim();
            String commandName = text.split("\\s+")[0].toLowerCase();

            // Normaliza: /help -> help
            if (commandName.startsWith("/")) {
                commandName = commandName.substring(1);
            }

            // Busca el bean correspondiente al comando (por nombre)
            BotCommand command = null;
            try {
                command = (BotCommand) context.getBean(commandName);
            } catch (Exception ignored) {
                // no hay bean con ese nombre: se caerá al DEFAULT_COMMANDS
            }

            String reply = (command != null) ? command.execute(update, client) : DEFAULT_COMMANDS;

            // Si el comando ya envió un mensaje (UI), no dupliques
            if (reply != null && !reply.isBlank()) {
                client.execute(new SendMessage(chatId, reply));
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // Cualquier fallo no-Telegram
            e.printStackTrace();
        }
    }

    private int safeParseInt(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (Exception e) { return fallback; }
    }
}
