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
Onboarding inicial. El bot te muestra un selector de lenguaje (12 lenguajes con botones inline). Al elegir uno, queda guardado y todos los demás comandos lo usarán automáticamente.

---

### `/issue [lenguaje] [nivel]`
Busca issues abiertas en GitHub por lenguaje y nivel de dificultad.

| Uso | Comportamiento |
|-----|---------------|
| `/issue` | Usa tu lenguaje guardado y muestra selector de nivel |
| `/issue java` | Muestra selector de nivel para Java |
| `/issue java principiante` | Busca issues de nivel principiante en Java |
| `/issue python intermedio` | Busca issues de nivel intermedio en Python |
| `/issue go avanzado` | Busca issues de nivel avanzado en Go |

**Niveles disponibles:**
- `principiante` → label `good first issue`
- `intermedio` → label `help wanted`
- `avanzado` → label `bug`

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
1. 🔍 Una selección de issues para explorar
2. 💪 Un ejercicio de Exercism
3. 🧠 Una pregunta de programación

Al completarlo se actualiza tu racha 🔥. Solo puede hacerse una vez por día.

---

### `/profile`
Muestra tu panel de progreso personal:
- 🔥 Racha de días consecutivos activos
- 💻 Lenguaje favorito configurado
- 💪 Ejercicios explorados
- 🔍 Issues revisadas
- 🧠 Quizzes completados
- ⭐ Puntos totales acumulados en quizzes

---

### `/resources [lenguaje]`
Lista de recursos curados para el lenguaje elegido:
- Documentación oficial
- Tutorial de inicio
- Track de Exercism
- Awesome List del lenguaje
- Subreddit de aprendizaje

| Uso | Comportamiento |
|-----|---------------|
| `/resources` | Usa tu lenguaje guardado |
| `/resources java` | Recursos de Java |
| `/resources python` | Recursos de Python |

**Lenguajes soportados:** java · python · javascript · typescript · go · rust · kotlin · ruby · csharp · cpp · swift · php

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
│   ├── StartCommand.java             # /start — onboarding con selector de lenguaje
│   ├── IssueCommand.java             # /issue — issues de GitHub por dificultad
│   ├── ExercisesCommand.java         # /exercises — ejercicios de Exercism
│   ├── QuestionsCommand.java         # /questions — quiz de 20 preguntas
│   ├── DailyCommand.java             # /daily — reto diario
│   ├── ProfileCommand.java           # /profile — estadísticas del usuario
│   ├── ResourcesCommand.java         # /resources — recursos curados
│   └── HelpCommand.java              # /help
├── service/
│   ├── GitHubIssueService.java       # GitHub Search API + caché por chat
│   ├── ExercismService.java          # Exercism API + caché por chat
│   ├── TriviaService.java            # Open Trivia DB API + estado del quiz por chat
│   └── UserSessionService.java       # Preferencias, estadísticas y racha por usuario
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

### APIs externas utilizadas

| API | Uso | Límite gratuito |
|-----|-----|-----------------|
| GitHub Search API | Buscar issues por lenguaje y label | 5000 req/hora (con token) |
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
