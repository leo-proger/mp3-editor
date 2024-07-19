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
            "(?i)\\(ru.soundmax.me\\)", "(?i)\\(AxeMusic.ru\\)", "(?i)\\(musmore.com\\)", "(?i)\\(remix-x.ru\\)",
            "(?i)\\(MP3Ball.ru\\)", "(?i)\\(Byfet.com\\)", "(?i)\\(EEMUSIC.ru\\)", "(?i)\\(Music Video\\)",
            "(?i)\\(Official Music Video\\)", "(?i)\\(Official Video\\)", "(?i)\\[Official Music Video\\]",
            "(?i)\\[Official Video\\]", "(?i)\\[Music Video\\]"
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

    private boolean isValidMp3Filename(String filename) {
        // TODO: Написать свою регулярку (текущее написано с помощью ChatGPT :))
        String regex = "^(?![/\\\\])(?=.*\\.mp3$)(?:([^/\\\\]+)_-_([^/\\\\]+)|([^/\\\\]+) - ([^/\\\\]+))\\.mp3$";
        return filename.matches(regex);
    }

    /**
     * Метод удаляет рекламу, находящуюся в BLACKLIST, игнорируя регистр, и удаляет в конце, перед ".mp3", все, кроме цифр, букв и закрывающей скобки (остатки от рекламы)
     */
    private void removeAd() throws Mp3FileFormatException {
        if (!isValidMp3Filename(newFilename)) {
            errorTracks.add(mp3File);
            throw new Mp3FileFormatException(mp3File);
        }
        // Удаляем все подстроки (реклама) из списка, пробелы, тире и нижнее подчеркивание перед ".mp3"
        newFilename = BLACKLIST.stream()
                .reduce(newFilename, (str, pattern) -> str.replaceAll(pattern, ""))
                .trim()
                .replaceAll("(?i)[ _-]+\\.mp3$", ".mp3");

    }

    /**
     * Метод форматирует имя mp3 файла согласно такому формату:
     * <p>
     * 1. Заменяются все пробелы на нижнее подчеркивание
     * <p>
     * 2. Заменяются все прочие разделители исполнителей, указанные в ARTIST_SEPARATORS, на запятую с пробелом
     * <p>
     * 3. Заменяется имя исполнителя на корректное, если требуется
     */
    private void formatMp3Filename() throws Mp3FileFormatException {
        if (!isValidMp3Filename(newFilename)) {
            errorTracks.add(mp3File);
            throw new Mp3FileFormatException(mp3File);
        }
        String formattedFilename = newFilename.replaceAll(" ", "_").replaceAll(",_", ", ");

        // Разделяем на часть с исполнителями и часть с названием трека
        String[] parts = formattedFilename.split("_-_");
        String left = parts[0];
        String right = parts[1];

        // Заменяем все прочие разделители исполнителей на запятую с пробелом
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
        // Преобразуем строку в объект файла, чтобы можно было работать с метаданными
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

        removeAd();
        formatMp3Filename();
        formatMetadata();

        // Переименование файла на файл с отформатированным именем
        Path newMp3File = mp3File.getParent().resolve(newFilename);
        Files.move(mp3File, newMp3File);

        // Добавляем текущий трек в список измененных треков
        changedTracks.add(newMp3File);
    }
}
