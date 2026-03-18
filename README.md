# DevBot — Bot de Telegram para Desarrolladores

Un bot de Telegram pensado para ayudarte a crecer como desarrollador: encuentra issues reales en GitHub, practica con ejercicios, pon a prueba tus conocimientos con quizzes y mantén una racha diaria de estudio.

---

## Requisitos previos

- Java 17+
- Maven 3.6+ (o usar `mvnw.cmd` incluido en el proyecto)
- Una cuenta de Telegram y un bot creado con [@BotFather](https://t.me/BotFather)
- Token de GitHub (opcional, aumenta el rate limit de 60 a 5000 req/hora)

---

## Configuración

Crea un archivo `.env` en la raíz del proyecto con estas tres variables:

```env
TELEGRAM_BOT_TOKEN=tu_token_del_bot
TELEGRAM_BOT_USERNAME=nombre_de_tu_bot
GITHUB_TOKEN=tu_github_personal_access_token
```

> El archivo `.env` está en `.gitignore` y nunca se sube al repositorio.

---

## Arrancar el bot

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / Mac
./mvnw spring-boot:run
```

Para generar el JAR y ejecutarlo:

```bash
mvnw.cmd clean package
java -jar target/TelegramBot-0.0.1-SNAPSHOT.jar
```

---

## Comandos disponibles

### `/start`
Onboarding inicial. El bot te muestra un selector de 12 lenguajes con botones inline.

**Flujo de 2 lenguajes favoritos:**
1. Eliges tu **lenguaje principal** (ej: Java)
2. El bot pregunta si quieres añadir un **segundo lenguaje** favorito
3. Puedes elegir un segundo o pulsar **⏭ Solo con un lenguaje** para continuar

Todos los demás comandos usarán tu lenguaje principal automáticamente.

---

### `/issue`
Busca issues abiertas en GitHub mediante un selector interactivo en **2 pasos**:

**Paso 1 — Idioma de las issues:**

| Botón | Descripción |
|-------|-------------|
| 🇪🇸 Español | Busca en repositorios etiquetados en español |
| 🇬🇧 English | Búsqueda estándar en inglés (por defecto) |

**Paso 2 — Tipo de issue:**

| Botón | Label de GitHub |
|-------|----------------|
| 🟢 Good First Issue | `good first issue` — ideal para contribuir por primera vez |
| 🤝 Help Wanted | `help wanted` — el proyecto pide ayuda activamente |
| 🐛 Bug | `bug` — errores confirmados que necesitan solución |
| 📋 Todas | Sin filtro de etiqueta |

Los resultados aparecen con paginación: ◀️ anterior · 🔄 refrescar · ▶️ siguiente

---

### `/exercises [lenguaje] [dificultad]`
Muestra ejercicios reales de [Exercism](https://exercism.org) con enlace directo para resolverlos.

| Uso | Comportamiento |
|-----|---------------|
| `/exercises` | Usa tu lenguaje guardado, todos los niveles |
| `/exercises python` | Ejercicios de Python, todos los niveles |
| `/exercises javascript principiante` | Ejercicios fáciles de JavaScript |
| `/exercises rust avanzado` | Ejercicios difíciles de Rust |

**Dificultades:** `principiante` · `intermedio` · `avanzado`

Los ejercicios muestran nombre, descripción y enlace directo a Exercism.

---

### `/questions`
Inicia un quiz de **20 preguntas de programación** obtenidas de [Open Trivia DB](https://opentdb.com).

- Las respuestas aparecen como botones (A / B / C / D)
- Tras cada respuesta el bot indica si acertaste y cuál era la correcta
- Incluye barra de progreso visual `[████░░░░░░]`
- Al finalizar muestra tu puntuación con medalla 🏆🥈🥉

---

### `/daily`
Tu **reto diario** combinado. Cada día te envía:
1. 🔍 Issues del día para explorar (siempre incluidas, con mensaje si no hay)
2. 💪 Un ejercicio de Exercism
3. 🧠 Una pregunta de programación

Al completarlo se actualiza tu racha 🔥. Solo puede hacerse una vez por día.

---

### `/profile`
Muestra tu panel de progreso personal:
- 🔥 Racha de días consecutivos activos
- 💻 Lenguajes favoritos configurados (hasta 2)
- 💪 Ejercicios explorados
- 🔍 Issues revisadas
- 🧠 Quizzes completados
- ⭐ Puntos totales acumulados en quizzes

---

### `/help`
Muestra un resumen de todos los comandos disponibles.

---

## Lenguajes soportados

| Lenguaje | Exercism | Issues GitHub |
|----------|----------|---------------|
| Java | ✅ | ✅ |
| Python | ✅ | ✅ |
| JavaScript | ✅ | ✅ |
| TypeScript | ✅ | ✅ |
| Go | ✅ | ✅ |
| Rust | ✅ | ✅ |
| Kotlin | ✅ | ✅ |
| Ruby | ✅ | ✅ |
| C# | ✅ | ✅ |
| C++ | ✅ | ✅ |
| Swift | ✅ | ✅ |
| PHP | ✅ | ✅ |

---

## Arquitectura

```
src/main/java/org/example/telegrambot/
├── bot/
│   ├── BotConsumer.java              # Long polling — recibe updates de Telegram
│   └── TelegramCommandHandler.java   # Dispatcher: enruta comandos y callbacks inline
├── commands/
│   ├── BotCommand.java               # Interfaz base
│   ├── StartCommand.java             # /start — onboarding con hasta 2 lenguajes favoritos
│   ├── IssueCommand.java             # /issue — selector idioma (🇪🇸/🇬🇧) + etiqueta
│   ├── ExercisesCommand.java         # /exercises — ejercicios de Exercism
│   ├── QuestionsCommand.java         # /questions — quiz de 20 preguntas
│   ├── DailyCommand.java             # /daily — reto diario
│   ├── ProfileCommand.java           # /profile — estadísticas del usuario
│   └── HelpCommand.java              # /help
├── service/
│   ├── GitHubIssueService.java       # GitHub Search API + filtro idioma + caché por chat
│   ├── ExercismService.java          # Exercism API + caché por chat
│   ├── TriviaService.java            # Open Trivia DB API + estado del quiz por chat
│   └── UserSessionService.java       # Hasta 2 lenguajes favoritos, stats y racha
├── ui/
│   ├── IssuesUI.java                 # Mensajes de issues con teclado inline paginado
│   ├── ExerciseUI.java               # Mensajes de ejercicios con teclado inline paginado
│   └── QuizUI.java                   # Mensajes del quiz con botones de respuesta
├── config/
│   ├── TelegramBotConfig.java        # Configuración del bot y registro de comandos
│   ├── TelegramBotProperties.java    # Propiedades de Telegram
│   └── GitHubProperties.java         # Propiedades de GitHub
└── utils/
    └── Constants.java                # Mensajes y constantes
```

### Flujo de callbacks inline

| Callback | Descripción |
|----------|-------------|
| `LANG:java` | Guarda lenguaje principal, pide segundo |
| `LANG2:python` | Guarda segundo lenguaje |
| `LANG_SKIP` | Confirma solo con el lenguaje principal |
| `ISSUE_HLANG:java:es` | Paso 1 de /issue — idioma elegido |
| `ISSUE_LABEL:java:es:gfi` | Paso 2 de /issue — etiqueta elegida, lanza búsqueda |
| `ISSUES_PAGE:2` | Paginación de issues |
| `ISSUES_REFRESH` | Refresca resultados de issues |
| `EX_PAGE:java:2` | Paginación de ejercicios |
| `EX_REFRESH:java` | Refresca ejercicios |
| `QA:0` / `QA:1` / `QA:2` / `QA:3` | Respuesta del quiz |

### APIs externas utilizadas

| API | Uso | Límite gratuito |
|-----|-----|-----------------|
| GitHub Search API | Buscar issues por lenguaje, etiqueta e idioma | 5000 req/hora (con token) |
| Exercism API v2 | Obtener ejercicios por track y dificultad | Sin límite conocido |
| Open Trivia DB | Preguntas de programación (categoría 18) | Sin límite conocido |

---

## Stack tecnológico

| Tecnología | Versión |
|-----------|---------|
| Java | 17 |
| Spring Boot | 3.5.6 |
| TelegramBots | 9.1.0 (long polling) |
| Spring WebFlux | 3.5.6 (WebClient para APIs) |
| Lombok | — |
| dotenv-java | 3.0.0 |
