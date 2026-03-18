package org.example.telegrambot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.menubutton.SetChatMenuButton;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.menubutton.MenuButtonCommands;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.example.telegrambot.bot.BotConsumer;

import java.util.List;

@Configuration
@Slf4j
public class TelegramBotConfig {

    @Bean
    TelegramClient telegramClient(TelegramBotProperties props) {
        return new OkHttpTelegramClient(props.getToken());
    }

    @Bean
    ApplicationRunner startBot(TelegramBotProperties props, BotConsumer consumer, TelegramClient client) {
        return args -> {
            TelegramBotsLongPollingApplication app = new TelegramBotsLongPollingApplication();
            app.registerBot(props.getToken(), consumer);

            // 1) Definir comandos que verás en el botón "Menu"
            List<BotCommand> commands = List.of(
                    new BotCommand("start",     "Configurar lenguaje favorito"),
                    new BotCommand("issue",     "Issues de GitHub por dificultad"),
                    new BotCommand("exercises", "Ejercicios reales de Exercism"),
                    new BotCommand("questions", "Quiz de 20 preguntas"),
                    new BotCommand("daily",     "Reto diario 🔥"),
                    new BotCommand("profile",   "Tu progreso y estadísticas"),
                    new BotCommand("resources", "Recursos curados por lenguaje"),
                    new BotCommand("help",      "Ayuda")
            );

            try {
                // 2) Registrar comandos del bot
                client.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));

                // 3) (Opcional) Forzar que el botón de menú muestre los comandos
                SetChatMenuButton setMenu = new SetChatMenuButton();
                setMenu.setMenuButton(new MenuButtonCommands());
                client.execute(setMenu);

                log.info("Bot commands registrados: {}", commands);
            } catch (TelegramApiException e) {
                log.error("Error registrando comandos del bot", e);
            }


        };
    }
}
