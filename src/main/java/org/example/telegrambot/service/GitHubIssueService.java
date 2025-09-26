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

    // Historial de issues ya mostradas por chatId (para no repetir en una sesión)
    private final Map<String, Set<String>> issuesMostradasPorChat = new ConcurrentHashMap<>();

    // --- NUEVO: caché de última búsqueda por chat ---
    private record SearchCtx(String language, String label, LocalDate sinceDate) {}
    private final Map<Long, SearchCtx> lastCtxByChat = new ConcurrentHashMap<>();
    private final Map<Long, List<Map<String, Object>>> lastIssuesByChat = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("Authorization", "Bearer " + gitHubProperties.getToken())
                .build();
    }

    /**
     * Primera búsqueda: fija un contexto aleatorio (fecha base) y cachea resultados.
     */
    public List<Map<String, Object>> findAndCache(Long chatId, String language, String label) {
        // fecha aleatoria dentro de los últimos 4 meses, pero fija para este contexto
        LocalDate start = LocalDate.now().minusMonths(4);
        long days = ChronoUnit.DAYS.between(start, LocalDate.now());
        LocalDate since = start.plusDays(new Random().nextInt((int) days + 1));

        lastCtxByChat.put(chatId, new SearchCtx(language, label, since));

        List<Map<String, Object>> result = searchIssues(language, label, since, 10);
        lastIssuesByChat.put(chatId, result);
        return result;
    }

    /**
     * Refresca usando el mismo contexto guardado (misma fecha base, mismo lang/label).
     * Si no hay contexto, hace uno por defecto.
     */
    public List<Map<String, Object>> refresh(Long chatId) {
        SearchCtx ctx = lastCtxByChat.get(chatId);
        if (ctx == null) {
            // valores por defecto para no romper callbacks si no hay contexto previo
            return findAndCache(chatId, "Java", "good first issue");
        }
        List<Map<String, Object>> result = searchIssues(ctx.language(), ctx.label(), ctx.sinceDate(), 10);
        lastIssuesByChat.put(chatId, result);
        return result;
    }

    /**
     * Últimos resultados cacheados (útil para paginación sin volver a llamar a GitHub).
     */
    public List<Map<String, Object>> getLastIssues(Long chatId) {
        return lastIssuesByChat.getOrDefault(chatId, Collections.emptyList());
    }

    /**
     * Búsqueda base en GitHub con rotación de página y evitando PRs.
     */
    private List<Map<String, Object>> searchIssues(String language, String label, LocalDate sinceDate, int perPage) {
        int maxPages = 5;
        int page = new Random().nextInt(maxPages) + 1;

        // Añadimos "is:issue" para excluir pull requests
        String query = String.format(
                "is:issue language:%s label:%s state:open created:>%s",
                language, label, sinceDate
        );

        List<Map<String, Object>> items = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/issues")
                        .queryParam("q", query)
                        .queryParam("sort", "created")
                        .queryParam("order", "desc")
                        .queryParam("per_page", perPage)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (List<Map<String, Object>>) response.get("items"))
                .block();

        if (items == null) return Collections.emptyList();

        // Filtrar issues ya mostradas a este chat (por id)
        // (esto controla repetición entre invocaciones, además de la rotación por página)
        List<Map<String, Object>> nuevas = new ArrayList<>(items.size());
        // usamos chatId lógico "GLOBAL" porque aquí no lo recibimos; este método se llama
        // desde findAndCache/refresh que van por chat. Si prefieres por chat real,
        // mueve este filtro allí y pásale el chatId.
        String chatKey = "GLOBAL";
        Set<String> mostradas = issuesMostradasPorChat.computeIfAbsent(chatKey, k -> ConcurrentHashMap.newKeySet());

        for (Map<String, Object> issue : items) {
            String id = String.valueOf(issue.get("id"));
            if (mostradas.add(id)) { // add devuelve false si ya existía
                nuevas.add(issue);
            }
        }

        // Si no hay nuevas (todas repetidas), devolvemos la página original
        if (nuevas.isEmpty()) {
            nuevas.addAll(items);
        }
        return nuevas;
    }

    // --- Método legacy por si lo sigues usando en algún sitio ---
    public List<Map<String, Object>> getIssues(String chatId, String language, String label) {
        List<Map<String, Object>> items = findAndCache(Long.valueOf(chatId), language, label);
        // mantener compat con tu filtro per-chat
        Set<String> mostradas = issuesMostradasPorChat.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet());
        List<Map<String, Object>> nuevas = new ArrayList<>();
        for (Map<String, Object> issue : items) {
            String id = String.valueOf(issue.get("id"));
            if (mostradas.add(id)) {
                nuevas.add(issue);
            }
        }
        return nuevas.isEmpty() ? items : nuevas;
    }

    /** Formateo simple (si no usas la UI de teclado). */
    public String formatIssues(List<Map<String, Object>> issues) {
        if (issues == null || issues.isEmpty()) return NOT_FOUND_ISSUES;

        StringBuilder sb = new StringBuilder(TEXT_ISSUES_FOUND);
        for (Map<String, Object> issue : issues) {
            String title = String.valueOf(issue.get("title"));
            String url = String.valueOf(issue.get("html_url"));
            sb.append("🔹 ").append(title).append("\n").append(url).append("\n\n");
        }
        return sb.toString();
    }
}
