package mp3handlers;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Mp3Formatter {
    private final static Set<String> BLACKLIST = Set.of(
            "(?i)\\(ru.soundmax.me\\)", "(?i)\\(AxeMusic.ru\\)", "(?i)\\(musmore.com\\)", "(?i)\\(remix-x.ru\\)",
            "(?i)\\(MP3Ball.ru\\)", "(?i)\\(Byfet.com\\)", "(?i)\\(EEMUSIC.ru\\)", "(?i)\\(Music Video\\)",
            "(?i)\\(Official Music Video\\)"
    );
    private static final Map<String, String> CORRECT_ARTIST_NAMES = new HashMap<>() {{
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
        put("oskalizator.", "oskalizator");
        put("vvpskvd.", "vvpskvd");
    }};

    // Исполнители, у которых не нужно убирать нижнее подчеркивание при добавлении в метаданные
    private static final Set<String> ARTISTS_EXCEPTIONS = Set.of(
            "boneles_s"
    );
    private static final String[] ARTIST_SEPARATORS = {
            "_x_",
            "_X_",
            "_&_",
            "_feat._",
            "_ft._",
            "_feat_",
            "_and_",
    };

    private static boolean isValidMp3Filename(String filename) {
        String regex = "^(?:.+\\\\)?(.+)_-_(.+)\\.mp3$";
        return filename.matches(regex);
    }

    /**
     * Метод удаляет рекламу, находящуюся в BLACKLIST, игнорируя регистр, и удаляет в конце, перед ".mp3", все, кроме цифр, букв и закрывающей скобки
     *
     * @param mp3FileName имя mp3 файла, из которого нужно убрать рекламу
     * @return строка, из которой удалена реклама
     */
    protected static String removeAd(String mp3FileName) throws Mp3FormatException {
        // Удаляем все подстроки из списка и лишние пробелы
        String result = BLACKLIST.stream()
                .reduce(mp3FileName, (str, pattern) -> str.replaceAll(pattern, ""))
                .trim();

        // Удаляем все символы перед .mp3, кроме букв и закрывающей скобки
        result = result.replaceAll("(?i)[^a-z0-9)]+\\.mp3$", ".mp3");

        if (result.split("_-_").length == 2 || result.split(" - ").length == 2) {
            return result;
        }
        throw new Mp3FormatException(mp3FileName);
    }

    /**
     * Метод форматирует имя mp3 файла согласно такому формату:
     * <p>
     * 1. Заменяются все пробелы на нижнее подчеркивание
     * <p>
     * 2. Заменяются все прочие разделители исполнителей, указанные в ARTIST_SEPARATORS, на запятую с пробелом
     * <p>
     * 3. Заменяется имя исполнителя на корректное, если требуется
     *
     * @param mp3FileName имя mp3 файла, которое нужно отформатировать
     * @return отформатированное имя mp3 файла
     */
    protected static String formatMp3Filename(String mp3FileName) {
        mp3FileName = mp3FileName.replaceAll(" ", "_").replaceAll(",_", ", ");

        // Разделяем на часть с исполнителями и часть с названием трека
        String[] parts = mp3FileName.split("_-_");
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


    /**
     * Метод добавляет mp3 файлу метаданные (название трека и исполнителей), исходя из имени mp3 файла. Форматирование производится согласно такому формату:
     * <p>
     * 1. Заменяются все нижние подчеркивания на пробелы
     * <p>
     * 2. Заменяется запятая на общепринятый разделитель исполнителей в метаданных - точка с запятой
     * @param mp3FileName имя mp3 файла, которому нужно добавить отформатированные метаданные
     */
    protected static void formatMetadata(String mp3FileName) throws IOException, Mp3FormatException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, CannotWriteException {
        // Проверяем соответствие имени mp3 файла определенному формату
        if (!isValidMp3Filename(mp3FileName)) {
            throw new Mp3FormatException(mp3FileName);
        }
        File file = new File(mp3FileName);
        if (!file.exists()) {
            throw new FileNotFoundException("Файл \"" + mp3FileName + "\" не найден");
        }
        AudioFile audioFile = AudioFileIO.read(file);
        Tag tag = audioFile.getTag();
        String[] parts = file.getName().replace(".mp3", "").split("_-_");

        // Проверяем есть ли исполнители, у которых не надо заменять нижнее подчеркивание на пробел
        Set<String> artistsForMetadata = new LinkedHashSet<>();
        for (String artist : parts[0].split(", ")) {
            if (ARTISTS_EXCEPTIONS.contains(artist)) {
                artistsForMetadata.add(artist);
            } else {
                artistsForMetadata.add(artist.replaceAll("_", " "));
            }
        }
        // Форматированные исполнители и название трека
        String formattedArtistForMetadata = String.join("; ", artistsForMetadata);
        String formattedTitleForMetadata = parts[1].replaceAll("_", " ");

        // Добавление стандартных метаданных, которые обязательно должны присутствовать и поддерживаться плеерами
        tag.setField(FieldKey.ARTIST, formattedArtistForMetadata);
        tag.setField(FieldKey.TITLE, formattedTitleForMetadata);

        // Добавление дополнительных метаданных для более широкой совместимости
        tag.setField(FieldKey.ARTISTS, formattedArtistForMetadata);
        if (tag instanceof AbstractID3v2Tag id3v2Tag) {
            id3v2Tag.setField(FieldKey.ARTIST, formattedArtistForMetadata);
            id3v2Tag.setField(FieldKey.ARTISTS, formattedArtistForMetadata);
        }
        // Сохранение изменений
        audioFile.commit();
    }

    public static String format(String mp3FileName) throws Mp3FormatException, IOException, CannotWriteException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException {
        String result = removeAd(mp3FileName);
        result = formatMp3Filename(result);
        formatMetadata(result);
        return result;
    }
}
