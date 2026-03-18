package org.example.telegrambot.commands;

import lombok.RequiredArgsConstructor;
import org.example.telegrambot.model.UserStats;
import org.example.telegrambot.repository.UserStatsRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("stats")
@RequiredArgsConstructor
public class StatsCommand implements BotCommand {

    private final UserStatsRepository repo;

    @Override
    public String execute(Update update, TelegramClient client) {
        List<UserStats> all = repo.findAll();

        long totalUsers   = all.size();
        long activeToday  = all.stream()
                .filter(u -> LocalDate.now().equals(u.getLastActiveDate()))
                .count();
        long activeWeek   = all.stream()
                .filter(u -> u.getLastActiveDate() != null &&
                             !u.getLastActiveDate().isBefore(LocalDate.now().minusDays(6)))
                .count();
        long totalExercises = all.stream().mapToLong(UserStats::getExercisesDone).sum();
        long totalIssues    = all.stream().mapToLong(UserStats::getIssuesExplored).sum();
        long totalQuizzes   = all.stream().mapToLong(UserStats::getQuizzesCompleted).sum();

        // Most popular language
        String topLang = all.stream()
                .filter(u -> u.getPrimaryLanguage() != null)
                .collect(Collectors.groupingBy(UserStats::getPrimaryLanguage, Collectors.counting()))
                .entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(e -> capitalize(e.getKey()) + " (" + e.getValue() + " usuarios)")
                .orElse("N/A");

        // Top streak
        String topStreak = all.stream()
                .filter(u -> u.getFirstName() != null)
                .max(Comparator.comparingInt(UserStats::getStreak))
                .map(u -> u.getFirstName() + " — " + u.getStreak() + " días 🔥")
                .orElse("N/A");

        return "📊 *Estadísticas de DevBot*\n" +
               "━━━━━━━━━━━━━━━━━━\n" +
               "👥 Usuarios totales: *" + totalUsers + "*\n" +
               "🟢 Activos hoy: *" + activeToday + "*\n" +
               "📅 Activos esta semana: *" + activeWeek + "*\n" +
               "━━━━━━━━━━━━━━━━━━\n" +
               "📈 *Actividad global*\n" +
               "💪 Ejercicios explorados: *" + totalExercises + "*\n" +
               "🔍 Issues revisadas: *" + totalIssues + "*\n" +
               "🧠 Quizzes completados: *" + totalQuizzes + "*\n" +
               "━━━━━━━━━━━━━━━━━━\n" +
               "🌍 Lenguaje más popular: *" + topLang + "*\n" +
               "🏆 Racha más larga: *" + topStreak + "*";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
