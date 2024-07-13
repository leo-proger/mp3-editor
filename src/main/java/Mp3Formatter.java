import java.util.*;

public class Mp3Formatter {
    private final static List<String> BLACKLIST = Arrays.asList(
            "(?i)\\(ru.soundmax.me\\)", "(?i)\\(AxeMusic.ru\\)", "(?i)\\(musmore.com\\)", "(?i)\\(remix-x.ru\\)",
            "(?i)\\(MP3Ball.ru\\)", "(?i)\\(Byfet.com\\)", "(?i)\\(EEMUSIC.ru\\)", "(?i) \\(Music Video\\)",
            "(?i) \\(Official Music Video\\)"
    );
    private static final Map<String, String> CORRECT_ARTIST_NAMES = new HashMap<String, String>() {{
        put("lxst_cxntury", "LXST_CXNTURY");
        put("vali_beats", "VALI$BEATS");
        put("valisbeats", "VALI$BEATS");
        put("my_lane", "my!lane");
        put("antxres", "AntXres");
        put("ya_h", "Ya$h");
        put("am_n", "Amøn");
        put("amon", "Amøn");
        put("voj", "VØJ");
        put("v_j", "VØJ");
        put("vj", "VØJ");
        put("scxr_soul", "SCXR_SOUL");
        put("swerve", "$werve");
        put("werve", "$werve");
        put("oldflop", "OLDFLOP");
        put("igres", "iGRES");
        put("finivoid", "FINIVOID");
        put("boneles_s", "boneles_s");
        put("oskalizator.", "oskalizator");
    }};

    private static final String[] ARTIST_SEPARATORS = {
            "_x_",
            "_X_",
            "_&_",
            "_feat._",
            "_ft._",
            "_feat_",
            "_and_",
    };

    /**
     * Метод удаляет рекламу, находящуюся в BLACKLIST, игнорируя регистр, и удаляет в конце, перед ".mp3", все, кроме цифр, букв и закрывающей скобки
     *
     * @param input имя mp3 файла, из которого нужно убрать рекламу
     * @return строка, из которой удалена реклама
     */
    public static String removeAd(String input) throws Mp3FormatException {
        // Удаляем все подстроки из списка и лишние пробелы
        String result = BLACKLIST.stream()
                .reduce(input, (str, pattern) -> str.replaceAll(pattern, ""))
                .trim();

        // Удаляем все символы перед .mp3, кроме букв и закрывающей скобки
        result = result.replaceAll("(?i)[^a-z0-9)]+\\.mp3$", ".mp3");

        if (result.split("_-_").length == 2 || result.split(" - ").length == 2) {
            return result;
        }
        throw new Mp3FormatException("Файл \"" + input + "\" не удается отформатировать");
    }

    /**
     * Форматирование имени mp3 файла производится согласно такому формату:
     * <p>
     * 1. Заменяются все пробелы на нижнее подчеркивание
     * <p>
     * 2. Заменяются все прочие разделители исполнителей, указанные в ARTIST_SEPARATORS, на запятую с пробелом
     * <p>
     * 3. Заменяется имя исполнителя на корректное, если требуется
     * @param input имя mp3 файла, которое нужно отформатировать
     * @return отформатированное имя mp3 файла
     */
    public static String formatMp3Filename(String input) {
        input = input.replaceAll(" ", "_").replaceAll(",_", ", ");

        // Разделяем на часть с исполнителями и часть с названием трека
        String[] parts = input.split("_-_");
        String left = parts[0];
        String right = parts[1];

        // Заменяем все прочие разделители исполнителей на ", "
        for (String artistSeparator : ARTIST_SEPARATORS) {
            if (left.contains(artistSeparator))
                left = left.replaceAll(artistSeparator, ", ");
        }

        // Заменяем имена исполнителей на корректные
        List<String> leftWithCorrectedArtistNames = new ArrayList<>();
        for (String artist : left.split(", ")) {
            if (CORRECT_ARTIST_NAMES.containsKey(artist.toLowerCase(Locale.ROOT))) {
                leftWithCorrectedArtistNames.add(
                        CORRECT_ARTIST_NAMES.getOrDefault(artist.toLowerCase(Locale.ROOT), artist.trim())
                );
            } else {
                leftWithCorrectedArtistNames.add(artist);
            }
        }
        // Возвращаем объединенные части с исполнителями и названием трека
        return String.join(", ", leftWithCorrectedArtistNames) + "_-_" + right;
    }
}
