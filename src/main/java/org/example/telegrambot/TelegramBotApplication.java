package org.example.telegrambot;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TelegramBotApplication {

    public static void main(String[] args) {

        // En local carga el .env; en producción (VPS/Railway) usa las variables del sistema
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()   // no falla si no existe el .env
                .load();

        setIfAbsent("TELEGRAM_BOT_TOKEN",    dotenv);
        setIfAbsent("TELEGRAM_BOT_USERNAME", dotenv);
        setIfAbsent("GITHUB_TOKEN",          dotenv);

        SpringApplication.run(TelegramBotApplication.class, args);
    }

    /**
     * Sets a system property only if it is not already defined as a real
     * environment variable. This way, variables set on the VPS (export VAR=...)
     * or in Railway/Docker take priority over the .env file.
     */
    private static void setIfAbsent(String key, Dotenv dotenv) {
        // If the OS already has the variable, do nothing
        if (System.getenv(key) != null) return;

        // Otherwise try the .env file
        String value = dotenv.get(key, null);
        if (value != null) {
            System.setProperty(key, value);
        }
    }
}
