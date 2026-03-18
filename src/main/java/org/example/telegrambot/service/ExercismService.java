package org.example.telegrambot.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExercismService {

    private WebClient webClient;

    /** Maps user-friendly language names to Exercism track slugs. */
    private static final Map<String, String> TRACK_MAP = Map.ofEntries(
            Map.entry("java",       "java"),
            Map.entry("python",     "python"),
            Map.entry("javascript", "javascript"),
            Map.entry("typescript", "typescript"),
            Map.entry("go",         "go"),
            Map.entry("rust",       "rust"),
            Map.entry("ruby",       "ruby"),
            Map.entry("kotlin",     "kotlin"),
            Map.entry("swift",      "swift"),
            Map.entry("php",        "php"),
            Map.entry("csharp",     "csharp"),
            Map.entry("c#",         "csharp"),
            Map.entry("cpp",        "cpp"),
            Map.entry("c++",        "cpp"),
            Map.entry("elixir",     "elixir"),
            Map.entry("scala",      "scala"),
            Map.entry("clojure",    "clojure"),
            Map.entry("haskell",    "haskell")
    );

    /** Cache: last exercise list per chat (for pagination callbacks). */
    private final Map<Long, List<Map<String, Object>>> lastExercisesByChat = new ConcurrentHashMap<>();
    private final Map<Long, String>                    lastLanguageByChat  = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl("https://exercism.org")
                .defaultHeader("User-Agent", "TelegramDevBot/1.0")
                .build();
    }

    public String resolveTrack(String language) {
        return TRACK_MAP.getOrDefault(language.toLowerCase(), language.toLowerCase());
    }

    /**
     * Fetch exercises for a language, optionally filtered by difficulty level.
     * Difficulty: principiante/beginner → easy | intermedio/intermediate → medium | avanzado/advanced → hard
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchAndCache(Long chatId, String language, String difficulty) {
        String track = resolveTrack(language);
        List<Map<String, Object>> exercises = new ArrayList<>();

        try {
            Map<String, Object> response = webClient.get()
                    .uri("/api/v2/tracks/" + track + "/exercises")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.get("exercises") instanceof List<?> list) {
                exercises = (List<Map<String, Object>>) list;
            }
        } catch (Exception e) {
            log.error("Error fetching Exercism exercises for track: {}", track, e);
            return Collections.emptyList();
        }

        if (difficulty != null && !difficulty.isBlank()) {
            String mapped = mapDifficulty(difficulty);
            exercises = exercises.stream()
                    .filter(e -> mapped.equalsIgnoreCase(String.valueOf(e.get("difficulty"))))
                    .collect(Collectors.toList());
        }

        // Exclude tutorial exercises to keep things interesting
        exercises = exercises.stream()
                .filter(e -> !"tutorial".equalsIgnoreCase(String.valueOf(e.get("type"))))
                .collect(Collectors.toList());

        Collections.shuffle(exercises);

        lastExercisesByChat.put(chatId, exercises);
        lastLanguageByChat.put(chatId, language.toLowerCase());
        return exercises;
    }

    public List<Map<String, Object>> getLastExercises(Long chatId) {
        return lastExercisesByChat.getOrDefault(chatId, Collections.emptyList());
    }

    public String getLastLanguage(Long chatId) {
        return lastLanguageByChat.getOrDefault(chatId, "java");
    }

    public String mapDifficulty(String input) {
        return switch (input.toLowerCase()) {
            case "principiante", "beginner", "easy", "fácil", "facil" -> "easy";
            case "intermedio", "intermediate", "medium", "medio"       -> "medium";
            case "avanzado", "advanced", "hard", "difícil", "dificil"  -> "hard";
            default -> input.toLowerCase();
        };
    }
}
