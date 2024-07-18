package com.github.Leo_Proger.mp3_editor.mp3handlers;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;

import static com.github.Leo_Proger.mp3_editor.main.Main.SOURCE_PATH;
import static com.github.Leo_Proger.mp3_editor.main.Main.TARGET_PATH;

public class Mp3Manager {
    private final static Mp3FileFormatter formatter;
    private static final Logger LOGGER;

    static {
        LOGGER = LoggerFactory.getLogger(Mp3FileFormatter.class);
        formatter = new Mp3FileFormatter();
    }

    /**
     * Метод форматирует каждый mp3 файл в SOURCE_PATH
     */
    private static void formatFiles() {
        try (Stream<Path> paths = Files.list(SOURCE_PATH)) {
            paths.filter(Files::isRegularFile).filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(".mp3")).forEach(path -> {
                try {
                    formatter.format(path);
                } catch (Mp3FileFormatException e) {
                    LOGGER.error("Ошибка форматирования файла \"{}\"", path.getFileName());
                } catch (Exception e) {
                    LOGGER.error("Ошибка при форматировании файла \"{}\"", path.getFileName());
                }
            });
        } catch (IOException e) {
            LOGGER.error("Ошибка при чтении папки");
        }
    }

    /**
     * Метод перемещает все mp3 файлы из SOURCE_PATH в TARGET_PATH
     */
    private static void moveMp3Files() {
        try (Stream<Path> paths = Files.list(SOURCE_PATH)) {
            paths.filter(Files::isRegularFile).filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(".mp3")).forEach(Mp3Manager::moveAndCheckMp3File);
        } catch (IOException e) {
            LOGGER.error("Ошибка при чтении папки");
        }
    }


    /**
     * Метод отдельно перемещает файл в папку с музыкой, проверяя на повреждения и существование в целевой папке
     *
     * @param source абсолютный путь к перемещаемому файлу
     */
    private static void moveAndCheckMp3File(Path source) {
        try {
            // Проверка файла на повреждения
            new Mp3File(source);

            // Перемещение файла, если он не существует в целевой папке
            if (Files.exists(TARGET_PATH.resolve(source.getFileName()))) {
                LOGGER.error("Файл \"{}\" уже существует в целевой папке.", source.getFileName());
            } else {
                Files.move(source, TARGET_PATH.resolve(source.getFileName()));
            }
        } catch (InvalidDataException | UnsupportedTagException e) {
            LOGGER.error("Файл \"{}\" поврежден.", source.getFileName());
        } catch (IOException e) {
            LOGGER.error("Ошибка при перемещении файла \"{}\"", source.getFileName());
        }
    }

    /**
     * Метод выводит логи о треках, которые были задействованы в форматировании
     */
    private static void printResults() {
        int countFiles = 1;
        Tag tag;
        try {
            for (Path file : Mp3FileFormatter.changedTracks) {
                AudioFile audioFile = AudioFileIO.read(file.toFile());
                tag = audioFile.getTag();

                LOGGER.info("""

                        {}. "{}"
                            Название: {}
                            Исполнители: {}
                        """, countFiles++, file.getFileName(), tag.getFirst(FieldKey.TITLE), tag.getFirst(FieldKey.ARTISTS));
            }
        } catch (Exception e) {
            LOGGER.error("Произошла ошибка при выводе измененных треков");
        }
        System.out.println("lol");
    }

    public static void run(boolean moveFiles) {
        formatFiles();
        if (moveFiles) {
            moveMp3Files();
        }
        printResults();
    }
}
