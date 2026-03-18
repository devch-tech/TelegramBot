package org.example.telegrambot.commands;

import lombok.RequiredArgsConstructor;
import org.example.telegrambot.service.UserSessionService;
// import org.springframework.stereotype.Component; // Command removed
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Map;

// @Component("resources") — command removed
@RequiredArgsConstructor
public class ResourcesCommand implements BotCommand {

    private final UserSessionService sessionService;

    private static final Map<String, String> RESOURCES = Map.ofEntries(
            Map.entry("java",
                    "☕ *Recursos Java*\n\n" +
                    "📖 [Docs Oracle](https://docs.oracle.com/javase/)\n" +
                    "🎓 [Tutorial oficial](https://dev.java/learn/)\n" +
                    "📚 [Exercism Java](https://exercism.org/tracks/java)\n" +
                    "🔥 [Awesome Java](https://github.com/akullpp/awesome-java)\n" +
                    "💬 [r/learnjava](https://reddit.com/r/learnjava)"),

            Map.entry("python",
                    "🐍 *Recursos Python*\n\n" +
                    "📖 [Docs oficiales](https://docs.python.org/3/)\n" +
                    "🎓 [Tutorial oficial](https://docs.python.org/3/tutorial/)\n" +
                    "📚 [Exercism Python](https://exercism.org/tracks/python)\n" +
                    "🔥 [Awesome Python](https://github.com/vinta/awesome-python)\n" +
                    "💬 [r/learnpython](https://reddit.com/r/learnpython)"),

            Map.entry("javascript",
                    "🌐 *Recursos JavaScript*\n\n" +
                    "📖 [MDN Web Docs](https://developer.mozilla.org/en-US/docs/Web/JavaScript)\n" +
                    "🎓 [javascript.info](https://javascript.info)\n" +
                    "📚 [Exercism JS](https://exercism.org/tracks/javascript)\n" +
                    "🔥 [Awesome JS](https://github.com/sorrycc/awesome-javascript)\n" +
                    "💬 [r/learnjavascript](https://reddit.com/r/learnjavascript)"),

            Map.entry("typescript",
                    "📘 *Recursos TypeScript*\n\n" +
                    "📖 [Docs oficiales](https://www.typescriptlang.org/docs/)\n" +
                    "🎓 [TS Playground](https://www.typescriptlang.org/play)\n" +
                    "📚 [Exercism TS](https://exercism.org/tracks/typescript)\n" +
                    "🔥 [Awesome TS](https://github.com/dzharii/awesome-typescript)"),

            Map.entry("go",
                    "🐹 *Recursos Go*\n\n" +
                    "📖 [Docs oficiales](https://go.dev/doc/)\n" +
                    "🎓 [Tour of Go](https://go.dev/tour/)\n" +
                    "📚 [Exercism Go](https://exercism.org/tracks/go)\n" +
                    "🔥 [Awesome Go](https://github.com/avelino/awesome-go)\n" +
                    "💬 [r/golang](https://reddit.com/r/golang)"),

            Map.entry("rust",
                    "🦀 *Recursos Rust*\n\n" +
                    "📖 [Docs oficiales](https://doc.rust-lang.org/)\n" +
                    "🎓 [The Rust Book](https://doc.rust-lang.org/book/)\n" +
                    "📚 [Exercism Rust](https://exercism.org/tracks/rust)\n" +
                    "🔥 [Awesome Rust](https://github.com/rust-unofficial/awesome-rust)\n" +
                    "💬 [r/rust](https://reddit.com/r/rust)"),

            Map.entry("kotlin",
                    "🎯 *Recursos Kotlin*\n\n" +
                    "📖 [Docs oficiales](https://kotlinlang.org/docs/)\n" +
                    "🎓 [Kotlin Koans](https://play.kotlinlang.org/koans/)\n" +
                    "📚 [Exercism Kotlin](https://exercism.org/tracks/kotlin)\n" +
                    "🔥 [Awesome Kotlin](https://github.com/KotlinBy/awesome-kotlin)"),

            Map.entry("ruby",
                    "💎 *Recursos Ruby*\n\n" +
                    "📖 [Docs oficiales](https://www.ruby-lang.org/en/documentation/)\n" +
                    "🎓 [Ruby Koans](http://rubykoans.com/)\n" +
                    "📚 [Exercism Ruby](https://exercism.org/tracks/ruby)\n" +
                    "🔥 [Awesome Ruby](https://github.com/markets/awesome-ruby)"),

            Map.entry("csharp",
                    "🔷 *Recursos C#*\n\n" +
                    "📖 [Docs Microsoft](https://learn.microsoft.com/en-us/dotnet/csharp/)\n" +
                    "🎓 [Microsoft Learn](https://learn.microsoft.com/en-us/dotnet/csharp/tour-of-csharp/)\n" +
                    "📚 [Exercism C#](https://exercism.org/tracks/csharp)\n" +
                    "🔥 [Awesome .NET](https://github.com/quozd/awesome-dotnet)"),

            Map.entry("cpp",
                    "⚡ *Recursos C++*\n\n" +
                    "📖 [cppreference](https://en.cppreference.com/)\n" +
                    "🎓 [learncpp.com](https://www.learncpp.com/)\n" +
                    "📚 [Exercism C++](https://exercism.org/tracks/cpp)\n" +
                    "🔥 [Awesome C++](https://github.com/fffaraz/awesome-cpp)"),

            Map.entry("swift",
                    "🍎 *Recursos Swift*\n\n" +
                    "📖 [Docs Swift](https://www.swift.org/documentation/)\n" +
                    "🎓 [Swift Book](https://docs.swift.org/swift-book/)\n" +
                    "📚 [Exercism Swift](https://exercism.org/tracks/swift)\n" +
                    "🔥 [Awesome Swift](https://github.com/matteocrippa/awesome-swift)"),

            Map.entry("php",
                    "🐘 *Recursos PHP*\n\n" +
                    "📖 [Docs oficiales](https://www.php.net/docs.php)\n" +
                    "🎓 [PHP The Right Way](https://phptherightway.com/)\n" +
                    "📚 [Exercism PHP](https://exercism.org/tracks/php)\n" +
                    "🔥 [Awesome PHP](https://github.com/ziadoz/awesome-php)")
    );

    @Override
    public String execute(Update update, TelegramClient client) {
        Long   chatId = update.getMessage().getChatId();
        String text   = update.getMessage().getText().trim();
        String[] tokens = text.split("\\s+");

        String language;
        if (tokens.length >= 2) {
            language = tokens[1].toLowerCase();
        } else if (sessionService.hasLanguage(chatId)) {
            language = sessionService.getLanguage(chatId);
        } else {
            return "💬 Especifica un lenguaje: `/resources java`\n" +
                   "O usa /start para configurar tu lenguaje favorito.";
        }

        // Normalize aliases
        if ("c#".equals(language)) language = "csharp";
        if ("c++".equals(language)) language = "cpp";

        String resources = RESOURCES.get(language);
        if (resources == null) {
            return "❌ No tengo recursos para *" + language + "*.\n\n" +
                   "Lenguajes disponibles: java, python, javascript, typescript, go, " +
                   "rust, kotlin, ruby, csharp, cpp, swift, php";
        }
        return resources;
    }
}
