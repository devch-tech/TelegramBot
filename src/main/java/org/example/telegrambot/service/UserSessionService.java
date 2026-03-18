package org.example.telegrambot.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserSessionService {

    private final Map<Long, String>    userLanguage    = new ConcurrentHashMap<>();
    private final Map<Long, Integer>   exercisesDone   = new ConcurrentHashMap<>();
    private final Map<Long, Integer>   issuesExplored  = new ConcurrentHashMap<>();
    private final Map<Long, Integer>   quizzesCompleted = new ConcurrentHashMap<>();
    private final Map<Long, Integer>   totalQuizScore  = new ConcurrentHashMap<>();
    private final Map<Long, LocalDate> lastActiveDate  = new ConcurrentHashMap<>();
    private final Map<Long, Integer>   streak          = new ConcurrentHashMap<>();
    private final Map<Long, LocalDate> lastDailyDate   = new ConcurrentHashMap<>();

    // --- Language preference ---

    public void setLanguage(Long chatId, String language) {
        userLanguage.put(chatId, language.toLowerCase());
    }

    public String getLanguage(Long chatId) {
        return userLanguage.get(chatId);
    }

    public String getLanguageOrDefault(Long chatId) {
        return userLanguage.getOrDefault(chatId, "java");
    }

    public boolean hasLanguage(Long chatId) {
        return userLanguage.containsKey(chatId);
    }

    // --- Activity & streak ---

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

    // --- Stats ---

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

    public int getExercisesDone(Long chatId)  { return exercisesDone.getOrDefault(chatId, 0); }
    public int getIssuesExplored(Long chatId) { return issuesExplored.getOrDefault(chatId, 0); }
    public int getQuizzesCompleted(Long chatId){ return quizzesCompleted.getOrDefault(chatId, 0); }
    public int getTotalQuizScore(Long chatId)  { return totalQuizScore.getOrDefault(chatId, 0); }

    // --- Daily challenge ---

    public boolean hasDoneDaily(Long chatId) {
        return LocalDate.now().equals(lastDailyDate.get(chatId));
    }

    public void markDailyDone(Long chatId) {
        lastDailyDate.put(chatId, LocalDate.now());
        recordActivity(chatId);
    }
}
