package org.example.telegrambot.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.telegrambot.config.GitHubProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.telegrambot.utils.Constants.NOT_FOUND_ISSUES;
import static org.example.telegrambot.utils.Constants.TEXT_ISSUES_FOUND;

@Service
@RequiredArgsConstructor
public class GitHubIssueService {

    private final GitHubProperties gitHubProperties;
    private WebClient webClient;

    private final Map<String, Set<String>> issuesMostradasPorChat = new ConcurrentHashMap<>();

    private record SearchCtx(String language, String label, LocalDate sinceDate, String humanLanguage) {}
    private final Map<Long, SearchCtx>                lastCtxByChat    = new ConcurrentHashMap<>();
    private final Map<Long, List<Map<String, Object>>> lastIssuesByChat = new ConcurrentHashMap<>();

    /** Label code → GitHub label text */
    public static final Map<String, String> LABEL_CODE_MAP = Map.of(
            "gfi", "good first issue",
            "hw",  "help wanted",
            "bug", "bug"
            // "all" → no label filter
    );

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("Authorization", "Bearer " + gitHubProperties.getToken())
                .build();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * First search: picks a random date context, caches results.
     * humanLanguage: "es" (Spanish) or "en" (English/default).
     * labelCode: "gfi", "hw", "bug", "all" — or raw GitHub label text.
     */
    public List<Map<String, Object>> findAndCache(Long chatId, String language,
                                                   String labelCode, String humanLanguage) {
        LocalDate start = LocalDate.now().minusMonths(4);
        long days = ChronoUnit.DAYS.between(start, LocalDate.now());
        LocalDate since = start.plusDays(new Random().nextInt((int) days + 1));

        lastCtxByChat.put(chatId, new SearchCtx(language, labelCode, since, humanLanguage));

        List<Map<String, Object>> result = searchIssues(language, labelCode, since, humanLanguage);
        lastIssuesByChat.put(chatId, result);
        return result;
    }

    /** Backward-compat: findAndCache without humanLanguage (defaults to "en"). */
    public List<Map<String, Object>> findAndCache(Long chatId, String language, String labelCode) {
        return findAndCache(chatId, language, labelCode, "en");
    }

    /** Refresh using the same cached context. */
    public List<Map<String, Object>> refresh(Long chatId) {
        SearchCtx ctx = lastCtxByChat.get(chatId);
        if (ctx == null) {
            return findAndCache(chatId, "java", "gfi", "en");
        }
        List<Map<String, Object>> result =
                searchIssues(ctx.language(), ctx.label(), ctx.sinceDate(), ctx.humanLanguage());
        lastIssuesByChat.put(chatId, result);
        return result;
    }

    public List<Map<String, Object>> getLastIssues(Long chatId) {
        return lastIssuesByChat.getOrDefault(chatId, Collections.emptyList());
    }

    // ── Internal search ───────────────────────────────────────────────────────

    private List<Map<String, Object>> searchIssues(String language, String labelCode,
                                                    LocalDate sinceDate, String humanLanguage) {
        int page = new Random().nextInt(5) + 1;

        // Resolve label
        String labelText = LABEL_CODE_MAP.getOrDefault(labelCode, labelCode);

        // Build query
        StringBuilder query = new StringBuilder();
        query.append("is:issue language:").append(language);
        if (labelText != null && !labelText.isBlank() && !"all".equals(labelCode)) {
            query.append(" label:\"").append(labelText).append("\"");
        }
        query.append(" state:open created:>").append(sinceDate);
        if ("es".equals(humanLanguage)) {
            query.append(" topic:español");
        }

        List<Map<String, Object>> items = fetchFromGitHub(query.toString(), page);

        // If Spanish search returns nothing, fall back without the topic filter
        if ((items == null || items.isEmpty()) && "es".equals(humanLanguage)) {
            StringBuilder fallbackQuery = new StringBuilder();
            fallbackQuery.append("is:issue language:").append(language);
            if (labelText != null && !labelText.isBlank() && !"all".equals(labelCode)) {
                fallbackQuery.append(" label:\"").append(labelText).append("\"");
            }
            fallbackQuery.append(" state:open created:>").append(sinceDate);
            items = fetchFromGitHub(fallbackQuery.toString(), page);
        }

        if (items == null) return Collections.emptyList();

        // Deduplicate already-shown issues globally
        Set<String> shown = issuesMostradasPorChat.computeIfAbsent("GLOBAL", k -> ConcurrentHashMap.newKeySet());
        List<Map<String, Object>> fresh = new ArrayList<>();
        for (Map<String, Object> issue : items) {
            if (shown.add(String.valueOf(issue.get("id")))) {
                fresh.add(issue);
            }
        }
        return fresh.isEmpty() ? items : fresh;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchFromGitHub(String query, int page) {
        try {
            return webClient.get()
                    .uri(uri -> uri
                            .path("/search/issues")
                            .queryParam("q", query)
                            .queryParam("sort", "created")
                            .queryParam("order", "desc")
                            .queryParam("per_page", 10)
                            .queryParam("page", page)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(r -> (List<Map<String, Object>>) r.get("items"))
                    .block();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // ── Legacy methods ────────────────────────────────────────────────────────

    public List<Map<String, Object>> getIssues(String chatId, String language, String label) {
        return findAndCache(Long.valueOf(chatId), language, label, "en");
    }

    public String formatIssues(List<Map<String, Object>> issues) {
        if (issues == null || issues.isEmpty()) return NOT_FOUND_ISSUES;
        StringBuilder sb = new StringBuilder(TEXT_ISSUES_FOUND);
        for (Map<String, Object> issue : issues) {
            sb.append("🔹 ").append(issue.get("title"))
              .append("\n").append(issue.get("html_url")).append("\n\n");
        }
        return sb.toString();
    }
}
