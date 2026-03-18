package org.example.telegrambot;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TelegramBotApplication {

    public static void main(String[] args) {

        // Cargar el .env antes de arrancar Spring Boot
        Dotenv dotenv = Dotenv.load();
        System.setProperty("TELEGRAM_BOT_TOKEN", dotenv.get("TELEGRAM_BOT_TOKEN"));
        System.setProperty("TELEGRAM_BOT_USERNAME", dotenv.get("TELEGRAM_BOT_USERNAME"));
        System.setProperty("GITHUB_TOKEN", dotenv.get("GITHUB_TOKEN"));

        SpringApplication.run(TelegramBotApplication.class, args);
    }
}
