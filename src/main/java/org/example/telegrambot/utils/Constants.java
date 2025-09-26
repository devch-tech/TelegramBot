package org.example.telegrambot.utils;

public class Constants {

    public static final String HELP_MESSAGE = """
          ✅*Comandos disponibles*
                /help — Ayuda
                /issue <lenguaje> <label> — Buscar issues de GitHub
                /exercises — Te envia diferentes ejercicios de programación para que los soluciones dependiendo del lenguaje que quieras
                /questions — La inteligencia artificial te lanza preguntas aleatorias de entrevistas
               \s
                *Ejemplos del command /issue:*
                /issue java "good first issue"
                /issue javascript bug
         \s""";


    public static final String DEFAULT_COMMANDS = "Comando no reconocido. Escribe /help para ver los comandos disponibles.";

    public static final String USO_COMMAND_ISSUE = "⚠️ Uso: /issue <language> <label>\nEjemplo: /issue java bug";

    public static final String NOT_FOUND_ISSUES = "❌ No encontré issues recientes con esos criterios.";

    public static final String TEXT_ISSUES_FOUND = "🚀 Issues encontradas:\n\n";
}
