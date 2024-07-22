package com.github.Leo_Proger.mp3_editor.mp3handlers;

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
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Mp3FileFormatter {
    /**
     * Список измененных треков, чтобы в конце программы вывести сводку
     */
    public final static List<Path> changedTracks = new LinkedList<>();

    /**
     * Треки, при форматировании которых выдает ошибку и, следовательно, их не нужно перемещать
     */
    public final static List<Path> errorTracks = new LinkedList<>();

    // Отключаем логирование библиотеки jaudiotagger
    static {
        var loggers = new Logger[]{Logger.getLogger("org.jaudiotagger")};
        for (Logger logger : loggers) {
            logger.setLevel(Level.OFF);
        }
    }

    /**
     * Реклама, которую нужно удалить из имени mp3 файла
     */
    private final static Set<String> BLACKLIST = Set.of(
            "\\(ru.soundmax.me\\)", "\\(AxeMusic.ru\\)", "\\(musmore.com\\)", "\\(remix-x.ru\\)",
            "\\(MP3Ball.ru\\)", "\\(Byfet.com\\)", "\\(EEMUSIC.ru\\)", "\\(Music Video\\)",
            "\\(Official Music Video\\)", "\\(Official Video\\)", "\\[Official Music Video\\]",
            "\\[Official Video\\]", "\\[Music Video\\]"
    );

    /**
     * Ключ - возможное написание исполнителя, значение - корректное написание исполнителя (при проверке возможного написания регистр не учитывается, то есть если в имени файла будет "swerve" или "Swerve", то в любом случае программа заменит это на "$werve")
     */
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
        put("westliberty's", "WESTLIBERTY'S");
        put("westlibertys", "WESTLIBERTY'S");
        put("westliberty s", "WESTLIBERTY'S");
    }};

    /**
     * Исполнители, у которых не нужно убирать нижнее подчеркивание при добавлении в метаданные
     */
    private static final Set<String> ARTISTS_EXCEPTIONS = Set.of(
            "boneles_s",
            "rex_incc"
    );

    /**
     * Разделители исполнителей, которые нужно заменить на точку с запятой, чтобы было единообразие
     */
    private static final String[] ARTIST_SEPARATORS = {
            "_x_",
            "_X_",
            "_&_",
            "_feat._",
            "_ft._",
            "_feat_",
            "_and_",
    };

    // Изначальный mp3 файл, имя которого копируется в newFilename и уже newFilename форматируется, в конце mp3File переименовывается на newFilename
    private Path mp3File;
    private String newFilename;

    /**
     * Проверяет регулярным выражением корректность формата имени mp3 файла перед дальнейшим форматированием.
     * Этот метод используется для валидации имени файла, чтобы предотвратить
     * возможные ошибки в последующих операциях форматирования.
     *
     * @param filename имя файла для проверки
     * @return {@code true}, если имя файла соответствует ожидаемому формату и может быть
     * обработано дальше, {@code false} в противном случае
     * @throws IllegalArgumentException если переданное имя файла является пустой строкой
     * @see Mp3FileFormatter#formatMp3Filename() Метод, который выполняет основной форматирование имени mp3 файла
     */
    public static boolean isValidMp3Filename(@NotNull String filename) {
        if (filename.isEmpty()) {
            throw new IllegalArgumentException("Имя mp3 файла не может быть пустым");
        }
        String regex = "^(([а-яА-Яa-zA-Z0-9()Ø\\-_.!$']+)(_[а-яА-Яa-zA-Z0-9()Ø\\-_.!$']+)*)(,\\s[а-яА-Яa-zA-Z0-9()Ø\\-_.!$']+(_[а-яА-Яa-zA-Z0-9()Ø\\-_.!$']+)*)*_-_([а-яА-Яa-zA-Z0-9()Ø\\-_.!$' ]+)\\.mp3$";
        return filename.matches(regex);
    }

    /**
     * Метод осуществляет предварительное форматирование:
     * <p>
     * 1. Удаляет рекламу, находящуюся в BLACKLIST
     * <p>
     * 2. Заменяет разделители, находящиеся в ARTIST_SEPARATORS, на ", "
     * <p>
     * Это нужно для того чтобы метод isValidMp3Filename() смог корректно проверить, можно ли название mp3 файла отформатировать без ошибок
     */
    private void preformatting() throws Mp3FileFormatException {
        // Проверка строки, что: это имя mp3 файла, у имени mp3 файла есть исполнитель и название, имя mp3 файла не имеет запрещенных символов
        if (!newFilename.matches("^([^\\\\/:*?\\\"<>|]+)(_-_| - )([^\\\\/:*?\\\"<>|]+)\\.mp3$")) {
            throw new Mp3FileFormatException(mp3File);
        }

        // Удаляем все подстроки (реклама) из списка, игнорируя регистр, а также пробелы, тире и нижнее подчеркивание перед ".mp3", которые остались после удаления рекламы
        newFilename = BLACKLIST.stream()
                .reduce(newFilename, (str, ad) -> str.replaceAll("(?i)" + ad, ""))
                .trim()
                .replaceAll("(?i)[ _-]+\\.mp3$", ".mp3");

        // Убираем пробелы и "поправляем" запятую
        String formattedFilename = newFilename
                .replaceAll(" ", "_")
                .replaceAll("[\\s_]*,[\\s_]*", ", ");

        // Разделяем на часть с исполнителями и часть с названием трека
        String[] parts = formattedFilename.split("_-_");
        String left = parts[0];
        String right = parts[1];

        // Заменяем все прочие разделители исполнителей на запятую с пробелом
        for (String artistSeparator : ARTIST_SEPARATORS) {
            if (left.contains(artistSeparator)) {
                left = left.replaceAll(artistSeparator, ", ");
            }
        }
        newFilename = left + "_-_" + right;
    }

    /**
     * Метод в имени mp3 файла заменяет имена исполнителей, находящиеся в CORRECT_ARTIST_NAMES, на корректное
     */
    private void formatMp3Filename() throws Mp3FileFormatException {
        preformatting();
        if (!isValidMp3Filename(newFilename)) {
            errorTracks.add(mp3File);
            throw new Mp3FileFormatException(mp3File);
        }

        // Разделяем на часть с исполнителями и часть с названием трека
        String[] parts = newFilename.split("_-_");
        String left = parts[0];
        String right = parts[1];

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
        // Определяем новое отформатированное имя файла
        newFilename = String.join(", ", leftWithCorrectedArtistNames) + "_-_" + right;
    }


    /**
     * Метод добавляет mp3 файлу метаданные (название трека и исполнителей), исходя из имени файла. Форматирование производится согласно такому формату:
     * <p>
     * 1. Заменяются все нижние подчеркивания на пробелы
     * <p>
     * 2. Заменяется запятая на общепринятый разделитель исполнителей в метаданных - точка с запятой
     */
    private void formatMetadata() throws IOException, Mp3FileFormatException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, CannotWriteException {
        if (!isValidMp3Filename(newFilename)) {
            errorTracks.add(mp3File);
            throw new Mp3FileFormatException(mp3File);
        }
        // Одновременно преобразуем строку в объект файла, чтобы можно было работать с метаданными, и проверяем файл на ошибки
        AudioFile audioFile = AudioFileIO.read(mp3File.toFile());
        Tag tag = audioFile.getTag();
        String[] parts = newFilename.replace(".mp3", "").split("_-_");

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

    /**
     * Метод запускает удаление рекламы, форматирование имени mp3 файла, форматирование метаданных mp3 файла и сохраняет изменения
     *
     * @param mp3File файл mp3, который нужно отформатировать
     * @throws Mp3FileFormatException ошибка форматирования mp3 файла
     */
    public void format(Path mp3File) throws Mp3FileFormatException, CannotWriteException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, IOException {
        this.mp3File = mp3File;
        newFilename = mp3File.getFileName().toString();

        formatMp3Filename();
        formatMetadata();

        // Переименование файла на файл с отформатированным именем
        Path newMp3File = mp3File.getParent().resolve(newFilename);
        Files.move(mp3File, newMp3File);

        // Добавляем текущий трек в список измененных треков
        changedTracks.add(newMp3File);
    }
}
