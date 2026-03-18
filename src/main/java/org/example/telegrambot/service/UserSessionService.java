package org.example.telegrambot.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserSessionService {

    private final Map<Long, List<String>> userLanguages   = new ConcurrentHashMap<>();
    private final Map<Long, Integer>      exercisesDone   = new ConcurrentHashMap<>();
    private final Map<Long, Integer>      issuesExplored  = new ConcurrentHashMap<>();
    private final Map<Long, Integer>      quizzesCompleted = new ConcurrentHashMap<>();
    private final Map<Long, Integer>      totalQuizScore  = new ConcurrentHashMap<>();
    private final Map<Long, LocalDate>    lastActiveDate  = new ConcurrentHashMap<>();
    private final Map<Long, Integer>      streak          = new ConcurrentHashMap<>();
    private final Map<Long, LocalDate>    lastDailyDate   = new ConcurrentHashMap<>();

    // ── Language preferences (up to 2) ──────────────────────────────────────

    /** Sets the first (primary) language, clearing any previous selection. */
    public void setFirstLanguage(Long chatId, String language) {
        List<String> list = new ArrayList<>();
        list.add(language.toLowerCase());
        userLanguages.put(chatId, list);
    }

    /** Adds a second language (ignored if already has 2 or if same as first). */
    public void addSecondLanguage(Long chatId, String language) {
        String lang = language.toLowerCase();
        List<String> list = userLanguages.computeIfAbsent(chatId, k -> new ArrayList<>());
        if (!list.contains(lang)) {
            if (list.size() >= 2) list.set(1, lang);
            else list.add(lang);
        }
    }

    /** Backward-compat: sets a single language (replaces current selection). */
    public void setLanguage(Long chatId, String language) {
        setFirstLanguage(chatId, language);
    }

    public String getPrimaryLanguage(Long chatId) {
        List<String> list = userLanguages.get(chatId);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
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
        List<String> list = userLanguages.get(chatId);
        return (list != null && !list.isEmpty()) ? list : List.of("java");
    }

    public boolean hasLanguage(Long chatId) {
        List<String> list = userLanguages.get(chatId);
        return list != null && !list.isEmpty();
    }

    // ── Activity & streak ────────────────────────────────────────────────────

    public void recordActivity(Long chatId) {
        LocalDate today = LocalDate.now();
        LocalDate last  = lastActiveDate.get(chatId);

        if (last == null) {
            streak.put(chatId, 1);
        } else if (last.plusDays(1).equals(today)) {
            streak.merge(chatId, 1, Integer::sum);
        } else if (!last.equals(today)) {
            streak.put(chatId, 1);
        }
        lastActiveDate.put(chatId, today);
    }

    public int getStreak(Long chatId) {
        return streak.getOrDefault(chatId, 0);
    }

    // ── Stats ────────────────────────────────────────────────────────────────

    public void addExerciseDone(Long chatId) {
        exercisesDone.merge(chatId, 1, Integer::sum);
        recordActivity(chatId);
    }

    public void addIssueExplored(Long chatId) {
        issuesExplored.merge(chatId, 1, Integer::sum);
        recordActivity(chatId);
    }

    public void addQuizCompleted(Long chatId, int score) {
        quizzesCompleted.merge(chatId, 1, Integer::sum);
        totalQuizScore.merge(chatId, score, Integer::sum);
        recordActivity(chatId);
    }

    public int getExercisesDone(Long chatId)   { return exercisesDone.getOrDefault(chatId, 0); }
    public int getIssuesExplored(Long chatId)  { return issuesExplored.getOrDefault(chatId, 0); }
    public int getQuizzesCompleted(Long chatId){ return quizzesCompleted.getOrDefault(chatId, 0); }
    public int getTotalQuizScore(Long chatId)  { return totalQuizScore.getOrDefault(chatId, 0); }

    // ── Daily challenge ──────────────────────────────────────────────────────

    public boolean hasDoneDaily(Long chatId) {
        return LocalDate.now().equals(lastDailyDate.get(chatId));
    }

    public void markDailyDone(Long chatId) {
        lastDailyDate.put(chatId, LocalDate.now());
        recordActivity(chatId);
    }
}
