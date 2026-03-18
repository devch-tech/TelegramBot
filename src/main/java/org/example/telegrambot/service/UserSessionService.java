package org.example.telegrambot.service;

import lombok.RequiredArgsConstructor;
import org.example.telegrambot.model.UserStats;
import org.example.telegrambot.repository.UserStatsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages user preferences and statistics, backed by an H2 file database
 * so data persists across bot restarts.
 *
 * Public API is identical to the previous in-memory version so no other
 * class needs to change.
 */
@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserStatsRepository repo;

    // ── Internal helpers ─────────────────────────────────────────────────────

    private UserStats load(Long chatId) {
        return repo.findById(chatId).orElseGet(() -> {
            UserStats u = new UserStats();
            u.setChatId(chatId);
            u.setRegisteredAt(LocalDate.now());
            return u;
        });
    }

    private UserStats save(UserStats u) {
        return repo.save(u);
    }

    // ── User registration (call from /start so we capture name) ─────────────

    public void registerOrUpdate(Long chatId, String firstName, String username) {
        UserStats u = load(chatId);
        u.setFirstName(firstName);
        u.setUsername(username);
        if (u.getRegisteredAt() == null) u.setRegisteredAt(LocalDate.now());
        save(u);
    }

    // ── Language preferences (up to 2) ──────────────────────────────────────

    public void setFirstLanguage(Long chatId, String language) {
        UserStats u = load(chatId);
        u.setPrimaryLanguage(language.toLowerCase());
        u.setSecondLanguage(null);
        save(u);
    }

    public void addSecondLanguage(Long chatId, String language) {
        String lang = language.toLowerCase();
        UserStats u = load(chatId);
        if (!lang.equals(u.getPrimaryLanguage())) {
            u.setSecondLanguage(lang);
            save(u);
        }
    }

    /** Backward-compat: sets a single language (replaces current). */
    public void setLanguage(Long chatId, String language) {
        setFirstLanguage(chatId, language);
    }

    public String getPrimaryLanguage(Long chatId) {
        return repo.findById(chatId).map(UserStats::getPrimaryLanguage).orElse(null);
    }

    public String getLanguageOrDefault(Long chatId) {
        String lang = getPrimaryLanguage(chatId);
        return lang != null ? lang : "java";
    }

    /** Backward-compat alias. */
    public String getLanguage(Long chatId) {
        return getLanguageOrDefault(chatId);
    }

    public List<String> getLanguages(Long chatId) {
        return repo.findById(chatId).map(u -> {
            List<String> list = new ArrayList<>();
            if (u.getPrimaryLanguage() != null) list.add(u.getPrimaryLanguage());
            if (u.getSecondLanguage()  != null) list.add(u.getSecondLanguage());
            return list.isEmpty() ? List.of("java") : list;
        }).orElse(List.of("java"));
    }

    public boolean hasLanguage(Long chatId) {
        return repo.findById(chatId)
                   .map(u -> u.getPrimaryLanguage() != null)
                   .orElse(false);
    }

    // ── Activity & streak ────────────────────────────────────────────────────

    public void recordActivity(Long chatId) {
        UserStats u    = load(chatId);
        LocalDate today = LocalDate.now();
        LocalDate last  = u.getLastActiveDate();

        if (last == null) {
            u.setStreak(1);
        } else if (last.plusDays(1).equals(today)) {
            u.setStreak(u.getStreak() + 1);
        } else if (!last.equals(today)) {
            u.setStreak(1);
        }
        u.setLastActiveDate(today);
        save(u);
    }

    public int getStreak(Long chatId) {
        return repo.findById(chatId).map(UserStats::getStreak).orElse(0);
    }

    // ── Stats ────────────────────────────────────────────────────────────────

    public void addExerciseDone(Long chatId) {
        UserStats u = load(chatId);
        u.setExercisesDone(u.getExercisesDone() + 1);
        save(u);
        recordActivity(chatId);
    }

    public void addIssueExplored(Long chatId) {
        UserStats u = load(chatId);
        u.setIssuesExplored(u.getIssuesExplored() + 1);
        save(u);
        recordActivity(chatId);
    }

    public void addQuizCompleted(Long chatId, int score) {
        UserStats u = load(chatId);
        u.setQuizzesCompleted(u.getQuizzesCompleted() + 1);
        u.setTotalQuizScore(u.getTotalQuizScore() + score);
        save(u);
        recordActivity(chatId);
    }

    public int getExercisesDone(Long chatId)    { return repo.findById(chatId).map(UserStats::getExercisesDone).orElse(0); }
    public int getIssuesExplored(Long chatId)   { return repo.findById(chatId).map(UserStats::getIssuesExplored).orElse(0); }
    public int getQuizzesCompleted(Long chatId) { return repo.findById(chatId).map(UserStats::getQuizzesCompleted).orElse(0); }
    public int getTotalQuizScore(Long chatId)   { return repo.findById(chatId).map(UserStats::getTotalQuizScore).orElse(0); }

    // ── Daily challenge ──────────────────────────────────────────────────────

    public boolean hasDoneDaily(Long chatId) {
        return repo.findById(chatId)
                   .map(u -> LocalDate.now().equals(u.getLastDailyDate()))
                   .orElse(false);
    }

    public void markDailyDone(Long chatId) {
        UserStats u = load(chatId);
        u.setLastDailyDate(LocalDate.now());
        save(u);
        recordActivity(chatId);
    }
}
