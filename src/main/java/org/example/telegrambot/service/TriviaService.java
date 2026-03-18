package org.example.telegrambot.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TriviaService {

    private WebClient webClient;

    /** Immutable snapshot of one quiz question. */
    public record QuizQuestion(String question, List<String> answers, int correctIndex) {}

    /**
     * State of an active quiz for one chat.
     * currentIdx = index of the question to show next (0-based).
     */
    public record QuizState(
            List<QuizQuestion> questions,
            int currentIdx,
            int score,
            Long messageId) {

        public QuizState withMessageId(Long msgId) {
            return new QuizState(questions, currentIdx, score, msgId);
        }

        public QuizState advance(boolean correct) {
            return new QuizState(questions, currentIdx + 1, score + (correct ? 1 : 0), null);
        }

        public boolean isFinished() {
            return currentIdx >= questions.size();
        }
    }

    private final Map<Long, QuizState> activeQuizzes = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl("https://opentdb.com")
                .build();
    }

    /** Fetch `amount` multiple-choice questions from Open Trivia DB (category 18 = Computers). */
    public List<QuizQuestion> fetchQuestions(int amount) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.get()
                    .uri(u -> u.path("/api.php")
                            .queryParam("amount", amount)
                            .queryParam("category", 18)
                            .queryParam("type", "multiple")
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return Collections.emptyList();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (results == null) return Collections.emptyList();

            List<QuizQuestion> questions = new ArrayList<>();
            for (Map<String, Object> r : results) {
                String question = HtmlUtils.htmlUnescape(String.valueOf(r.get("question")));
                String correct  = HtmlUtils.htmlUnescape(String.valueOf(r.get("correct_answer")));

                @SuppressWarnings("unchecked")
                List<String> incorrect = ((List<String>) r.get("incorrect_answers")).stream()
                        .map(HtmlUtils::htmlUnescape)
                        .collect(Collectors.toList());

                List<String> answers = new ArrayList<>(incorrect);
                answers.add(correct);
                Collections.shuffle(answers);
                int correctIdx = answers.indexOf(correct);

                questions.add(new QuizQuestion(question, answers, correctIdx));
            }
            return questions;

        } catch (Exception e) {
            log.error("Error fetching trivia questions", e);
            return Collections.emptyList();
        }
    }

    /** Start a new 20-question quiz for the given chat, replacing any active quiz. */
    public QuizState startQuiz(Long chatId) {
        List<QuizQuestion> questions = fetchQuestions(20);
        QuizState state = new QuizState(questions, 0, 0, null);
        activeQuizzes.put(chatId, state);
        return state;
    }

    public QuizState getQuizState(Long chatId)          { return activeQuizzes.get(chatId); }
    public void updateQuizState(Long chatId, QuizState s) { activeQuizzes.put(chatId, s); }
    public void endQuiz(Long chatId)                    { activeQuizzes.remove(chatId); }
    public boolean hasActiveQuiz(Long chatId)           { return activeQuizzes.containsKey(chatId); }
}
