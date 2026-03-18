package org.example.telegrambot.commands;

import lombok.RequiredArgsConstructor;
import org.example.telegrambot.service.UserSessionService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

@Component("start")
@RequiredArgsConstructor
public class StartCommand implements BotCommand {

    private final UserSessionService sessionService;

    public static final String[][] LANGUAGES = {
            {"☕ Java",        "java"},
            {"🐍 Python",      "python"},
            {"🌐 JavaScript",  "javascript"},
            {"📘 TypeScript",  "typescript"},
            {"🐹 Go",          "go"},
            {"🦀 Rust",        "rust"},
            {"💎 Ruby",        "ruby"},
            {"🎯 Kotlin",      "kotlin"},
            {"🍎 Swift",       "swift"},
            {"🐘 PHP",         "php"},
            {"🔷 C#",          "csharp"},
            {"⚡ C++",         "cpp"}
    };

    @Override
    public String execute(Update update, TelegramClient client) {
        Long   chatId    = update.getMessage().getChatId();
        String firstName = update.getMessage().getFrom().getFirstName();
        String username  = update.getMessage().getFrom().getUserName();

        // Persist user so they appear in /stats
        sessionService.registerOrUpdate(chatId, firstName, username);

        String text = "👋 ¡Hola, *" + firstName + "*! Bienvenido a *DevBot* 🤖\n\n" +
                "Soy tu asistente personal para crecer como desarrollador:\n\n" +
                "🔍 *Issues* de GitHub filtradas por idioma y etiqueta\n" +
                "💪 *Ejercicios* reales de Exercism\n" +
                "🧠 *Quiz* de 20 preguntas de programación\n" +
                "📅 *Reto diario* para mantener tu racha 🔥\n" +
                "📊 *Perfil* con tu progreso y estadísticas\n\n" +
                "Puedes guardar *hasta 2 lenguajes favoritos*.\n" +
                "¿Cuál es tu lenguaje *principal*?";

        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        msg.setParseMode("Markdown");
        msg.setReplyMarkup(buildKeyboard("LANG", null));

        try {
            client.execute(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Builds the language selection keyboard.
     * @param callbackPrefix "LANG" for first language, "LANG2" for second.
     * @param excludeLang    Language key to exclude (e.g. already selected first lang). Null = no exclusion.
     */
    public static InlineKeyboardMarkup buildKeyboard(String callbackPrefix, String excludeLang) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        List<String[]> filtered = new ArrayList<>();

        for (String[] lang : LANGUAGES) {
            if (!lang[1].equals(excludeLang)) {
                filtered.add(lang);
            }
        }

        for (int i = 0; i < filtered.size(); i += 2) {
            InlineKeyboardRow row = new InlineKeyboardRow();

            InlineKeyboardButton b1 = new InlineKeyboardButton(filtered.get(i)[0]);
            b1.setCallbackData(callbackPrefix + ":" + filtered.get(i)[1]);
            row.add(b1);

            if (i + 1 < filtered.size()) {
                InlineKeyboardButton b2 = new InlineKeyboardButton(filtered.get(i + 1)[0]);
                b2.setCallbackData(callbackPrefix + ":" + filtered.get(i + 1)[1]);
                row.add(b2);
            }
            rows.add(row);
        }

        // Add "Skip" button at the bottom if this is for the second language
        if ("LANG2".equals(callbackPrefix)) {
            InlineKeyboardButton skip = new InlineKeyboardButton("⏭ Solo con un lenguaje");
            skip.setCallbackData("LANG_SKIP");
            InlineKeyboardRow skipRow = new InlineKeyboardRow();
            skipRow.add(skip);
            rows.add(skipRow);
        }

        InlineKeyboardMarkup kb = new InlineKeyboardMarkup(rows);
        kb.setKeyboard(rows);
        return kb;
    }

    /** Backward-compat for callers that used the old single-arg signature. */
    public static InlineKeyboardMarkup buildLanguageKeyboard() {
        return buildKeyboard("LANG", null);
    }
}
