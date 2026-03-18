package org.example.telegrambot.utils;

public class Constants {

    public static final String HELP_MESSAGE =
            "❓ *Comandos disponibles*\n\n" +
            "🏠 /start — Configura hasta *2 lenguajes favoritos*\n\n" +
            "🔍 /issue — Issues de GitHub\n" +
            "  _Elige idioma (🇪🇸/🇬🇧) y etiqueta (Good First Issue, Help Wanted…)_\n\n" +
            "💪 /exercises `<lenguaje>` — Ejercicios reales de Exercism\n" +
            "  _Ej: /exercises python principiante_\n\n" +
            "🧠 /questions — Quiz de 20 preguntas de programación\n\n" +
            "📅 /daily — Tu reto diario (issue + ejercicio + pregunta)\n\n" +
            "📊 /profile — Tu progreso, racha y estadísticas\n\n" +
            "_Tip: usa /start para guardar tus lenguajes y los comandos los usarán automáticamente_ 🚀";

    public static final String DEFAULT_COMMANDS =
            "Comando no reconocido. Escribe /help para ver todos los comandos disponibles.";

    public static final String NOT_FOUND_ISSUES =
            "❌ No encontré issues recientes con esos criterios. Prueba con otro nivel o lenguaje.";

    public static final String TEXT_ISSUES_FOUND = "🚀 Issues encontradas:\n\n";
}
