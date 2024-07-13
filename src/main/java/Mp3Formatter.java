import java.util.Arrays;
import java.util.List;

public class Mp3Formatter {
    private final static List<String> BLACKLIST = Arrays.asList(
            "(?i)\\(ru.soundmax.me\\)", "(?i)\\(AxeMusic.ru\\)", "(?i)\\(musmore.com\\)", "(?i)\\(remix-x.ru\\)",
            "(?i)\\(MP3Ball.ru\\)", "(?i)\\(Byfet.com\\)", "(?i)\\(EEMUSIC.ru\\)", "(?i) \\(Music Video\\)",
            "(?i) \\(Official Music Video\\)"
    );


    /**
     * Метод удаляет рекламу, находящуюся в BLACKLIST, игнорируя регистр, и удаляет в конце, перед ".mp3", все, кроме цифр, букв и закрывающей скобки
     *
     * @param input имя mp3 файла, из которого нужно убрать рекламу
     * @return строка, из которой удалена реклама
     */
    static String removeAd(String input) throws Mp3FormatException {
        // Удаляем все подстроки из списка и лишние пробелы
        String result = BLACKLIST.stream()
                .reduce(input, (str, pattern) -> str.replaceAll(pattern, ""))
                .trim();

        // Удаляем все символы перед .mp3, кроме букв и закрывающей скобки
        result = result.replaceAll("(?i)[^a-z0-9)]+\\.mp3$", ".mp3");

        if (result.split("_-_").length != 2 || result.split(" - ").length != 2)
            throw new Mp3FormatException("Файл \"" + input + "\" не удается отформатировать");

        return result;
    }
}
