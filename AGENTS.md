# TelegramBot - Guía para Agentes de Código

## Descripción General del Proyecto

TelegramBot es una aplicación Spring Boot que implementa un bot de Telegram para ayudar a desarrolladores a encontrar issues de GitHub, ejercicios de programación y preguntas de entrevista. El bot utiliza el modo long polling para recibir actualizaciones de Telegram y se integra con la API de GitHub para buscar issues abiertos.

### Funcionalidades Principales

- **Buscar issues de GitHub**: Comando `/issue <lenguaje> <label>` - Busca issues abiertos en GitHub filtrados por lenguaje de programación y etiqueta.
- **Ayuda**: Comando `/help` - Muestra la lista de comandos disponibles.
- **Ejercicios**: Comando `/exercises` - (Placeholder) Envía ejercicios de programación.
- **Preguntas**: Comando `/questions` - (Placeholder) Envía preguntas de entrevista técnica.

## Stack Tecnológico

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 17 | Lenguaje de programación |
| Spring Boot | 3.5.6 | Framework de aplicación |
| Maven | 3.x+ | Gestión de dependencias y build |
| TelegramBots | 9.1.0 | Integración con API de Telegram |
| WebFlux | 3.5.6 | Cliente HTTP para llamadas a GitHub API |
| Lombok | - | Reducción de boilerplate |
| PostgreSQL | - | Base de datos (runtime) |
| dotenv-java | 3.0.0 | Carga de variables de entorno desde `.env` |

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/org/example/telegrambot/
│   │   ├── TelegramBotApplication.java    # Punto de entrada de la aplicación
│   │   ├── bot/
│   │   │   ├── BotConsumer.java           # Consumidor de actualizaciones de Telegram
│   │   │   └── TelegramCommandHandler.java # Dispatcher de comandos y callbacks
│   │   ├── commands/                      # Implementaciones de comandos del bot
│   │   │   ├── BotCommand.java            # Interfaz base para comandos
│   │   │   ├── HelpCommand.java           # Implementación de /help
│   │   │   ├── IssueCommand.java          # Implementación de /issue
│   │   │   ├── ExercisesCommand.java      # Implementación de /exercises
│   │   │   └── QuestionsCommand.java      # Implementación de /questions
│   │   ├── config/                        # Configuración de Spring
│   │   │   ├── TelegramBotConfig.java     # Configuración del bot y registro de comandos
│   │   │   ├── TelegramBotProperties.java # Propiedades del bot (token, username)
│   │   │   └── GitHubProperties.java      # Propiedades de GitHub (token)
│   │   ├── service/
│   │   │   └── GitHubIssueService.java    # Lógica de búsqueda de issues en GitHub
│   │   ├── ui/
│   │   │   └── IssuesUI.java              # Construcción de UI con teclado inline
│   │   └── utils/
│   │       └── Constants.java             # Constantes y mensajes del bot
│   └── resources/
│       └── application.properties         # Configuración de Spring Boot
└── test/
    └── java/org/example/telegrambot/
        └── TelegramBotApplicationTests.java # Tests básicos
```

## Configuración y Variables de Entorno

El proyecto utiliza un archivo `.env` en la raíz para las variables de entorno sensibles:

```env
TELEGRAM_BOT_TOKEN=tu_token_del_bot_de_telegram
TELEGRAM_BOT_USERNAME=nombre_de_tu_bot
GITHUB_TOKEN=tu_github_personal_access_token
```

**Importante**: El archivo `.env` está en `.gitignore` y no debe ser commiteado.

Las variables se cargan en `TelegramBotApplication.java` usando `dotenv-java` antes de iniciar Spring Boot.

## Comandos de Build y Ejecución

### Requisitos Previos
- Java 17 o superior
- Maven 3.6+ (o usar `./mvnw` / `mvnw.cmd` incluido)

### Comandos Maven

```bash
# Compilar el proyecto
./mvnw clean compile

# Ejecutar tests
./mvnw test

# Empaquetar (generar JAR)
./mvnw clean package

# Ejecutar la aplicación
./mvnw spring-boot:run

# O ejecutar el JAR generado
java -jar target/TelegramBot-0.0.1-SNAPSHOT.jar
```

### En Windows
```cmd
mvnw.cmd clean compile
mvnw.cmd test
mvnw.cmd clean package
mvnw.cmd spring-boot:run
```

## Arquitectura del Bot

### Flujo de Mensajes

1. **BotConsumer** implementa `LongPollingSingleThreadUpdateConsumer` y recibe actualizaciones de Telegram.
2. **TelegramCommandHandler** procesa las actualizaciones:
   - Maneja callbacks de teclado inline (paginación, refresh)
   - Enruta comandos de texto a la implementación correspondiente
3. **Comandos** implementan la interfaz `BotCommand` y se registran como beans de Spring.
4. **GitHubIssueService** realiza las búsquedas en GitHub API usando WebClient.
5. **IssuesUI** construye los mensajes con teclado inline para navegación.

### Patrón de Comandos

Los comandos se implementan como beans de Spring con el nombre del comando:

```java
@Component("help")
public class HelpCommand implements BotCommand {
    @Override
    public String execute(Update update, TelegramClient client) {
        return HELP_MESSAGE;
    }
}
```

El handler busca el bean por nombre usando `ApplicationContext.getBean(commandName)`.

### Caché de Búsquedas

El servicio `GitHubIssueService` mantiene una caché por chat:
- `lastCtxByChat`: Contexto de la última búsqueda (lenguaje, etiqueta, fecha base)
- `lastIssuesByChat`: Resultados de la última búsqueda para paginación

Esto permite navegar por páginas y refrescar sin hacer nuevas llamadas a GitHub.

## Guía de Estilo de Código

### Convenciones

- **Paquetes**: Notación de dominio invertido (`org.example.telegrambot`)
- **Clases**: PascalCase
- **Métodos y variables**: camelCase
- **Constantes**: UPPER_SNAKE_CASE

### Uso de Lombok

El proyecto utiliza extensivamente Lombok:
- `@RequiredArgsConstructor` para inyección de dependencias
- `@Getter` / `@Setter` para propiedades
- `@Slf4j` para logging

### Inyección de Dependencias

Preferir inyección por constructor (vía `@RequiredArgsConstructor`) sobre `@Autowired`:

```java
@Service
@RequiredArgsConstructor
public class GitHubIssueService {
    private final GitHubProperties gitHubProperties;
    private final WebClient webClient;
}
```

## Testing

El proyecto tiene configuración básica de tests con JUnit 5:

```bash
# Ejecutar todos los tests
./mvnw test
```

El test actual (`TelegramBotApplicationTests`) solo verifica que el contexto de Spring carga correctamente.

### Estrategia de Testing Recomendada

1. Tests unitarios para servicios (mockeando WebClient)
2. Tests de integración para el flujo de comandos
3. Tests de contrato para la API de GitHub

## Consideraciones de Seguridad

1. **Tokens**: Nunca hardcodear tokens en el código fuente. Usar siempre variables de entorno.
2. **GitHub Token**: El token debe tener permisos mínimos necesarios (solo lectura pública).
3. **Rate Limiting**: GitHub API tiene límites de rate limit (60 requests/hora sin auth, 5000 con auth).
4. **Validación de Input**: El comando `/issue` valida que se proporcionen lenguaje y etiqueta.

## Comandos del Bot Disponibles

| Comando | Descripción | Ejemplo |
|---------|-------------|---------|
| `/help` | Muestra ayuda | `/help` |
| `/issue <lenguaje> <label>` | Busca issues de GitHub | `/issue java "good first issue"` |
| `/exercises` | Ejercicios de programación (WIP) | `/exercises` |
| `/questions` | Preguntas de entrevista (WIP) | `/questions` |

## Notas de Desarrollo

- Los comandos `/exercises` y `/questions` están implementados como placeholders y devuelven cadenas vacías.
- El bot utiliza teclados inline para la navegación de issues (paginación y refresh).
- Las issues se filtran para evitar mostrar Pull Requests (`is:issue` en la query).
- Se implementa rotación aleatoria de páginas para diversidad de resultados.
- El histórico de issues mostradas se mantiene en memoria para evitar repeticiones.

## Despliegue

La aplicación se puede desplegar como:
1. **JAR ejecutable**: `java -jar target/TelegramBot-0.0.1-SNAPSHOT.jar`
2. **Contenedor Docker**: Crear Dockerfile basado en imagen Java 17
3. **Servicio en la nube**: Compatible con Heroku, Railway, AWS, etc.

### Requisitos de Runtime
- Java 17
- Variables de entorno configuradas (o archivo `.env` presente)
- Conexión a Internet para APIs de Telegram y GitHub
