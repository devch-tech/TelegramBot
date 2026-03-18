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

    private static final String[][] LANGUAGES = {
            {"☕ Java",        "LANG:java"},
            {"🐍 Python",      "LANG:python"},
            {"🌐 JavaScript",  "LANG:javascript"},
            {"📘 TypeScript",  "LANG:typescript"},
            {"🐹 Go",          "LANG:go"},
            {"🦀 Rust",        "LANG:rust"},
            {"💎 Ruby",        "LANG:ruby"},
            {"🎯 Kotlin",      "LANG:kotlin"},
            {"🍎 Swift",       "LANG:swift"},
            {"🐘 PHP",         "LANG:php"},
            {"🔷 C#",          "LANG:csharp"},
            {"⚡ C++",         "LANG:cpp"}
    };

    @Override
    public String execute(Update update, TelegramClient client) {
        Long   chatId    = update.getMessage().getChatId();
        String firstName = update.getMessage().getFrom().getFirstName();

        String text = "👋 ¡Hola, *" + firstName + "*! Bienvenido a *DevBot* 🤖\n\n" +
                "Soy tu asistente personal para crecer como desarrollador:\n\n" +
                "🔍 *Issues* de GitHub por nivel de dificultad\n" +
                "💪 *Ejercicios* reales de Exercism\n" +
                "🧠 *Quiz* de 20 preguntas de programación\n" +
                "📅 *Reto diario* para mantener tu racha 🔥\n" +
                "📊 *Perfil* con tu progreso y estadísticas\n" +
                "📚 *Recursos* curados por lenguaje\n\n" +
                "¿Cuál es tu lenguaje principal?";

        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        msg.setParseMode("Markdown");
        msg.setReplyMarkup(buildLanguageKeyboard());

        try {
            client.execute(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static InlineKeyboardMarkup buildLanguageKeyboard() {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (int i = 0; i < LANGUAGES.length; i += 2) {
            InlineKeyboardRow row = new InlineKeyboardRow();

            InlineKeyboardButton b1 = new InlineKeyboardButton(LANGUAGES[i][0]);
            b1.setCallbackData(LANGUAGES[i][1]);
            row.add(b1);

            if (i + 1 < LANGUAGES.length) {
                InlineKeyboardButton b2 = new InlineKeyboardButton(LANGUAGES[i + 1][0]);
                b2.setCallbackData(LANGUAGES[i + 1][1]);
                row.add(b2);
            }
            rows.add(row);
        }
        InlineKeyboardMarkup kb = new InlineKeyboardMarkup(rows);
        kb.setKeyboard(rows);
        return kb;
    }
}
