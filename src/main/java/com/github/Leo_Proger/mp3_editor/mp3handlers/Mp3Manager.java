package com.github.Leo_Proger.mp3_editor.mp3handlers;

import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
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
    private static final String FILE_ALREADY_EXISTS_MSG = "Файл уже существует в целевой папке \"{}\"";
    private static final String ERROR_MOVING_FILE_MSG = "Ошибка при перемещении файла \"{}\"";
    private static final String FOLDER_INTERACTION_ERROR_MSG = "Ошибка взаимодействия с папкой \"{}\"";
    private static final String FILE_FORMATTING_ERROR_MSG = "Ошибка форматирования файла \"{}\"";
    private static final String FILE_CORRUPTED_ERROR_MSG = "Файл поврежден \"{}\"";
    private static final String UNKNOWN_ERROR_FORMATTING_FILE_MSG = "Неизвестная ошибка при форматировании файла \"{}\"";
    private static final String FOLDER_READING_ERROR_MSG = "Ошибка при чтении папки \"{}\"";

    private static final Mp3FileFormatter formatter;
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
            paths.filter(Files::isRegularFile) // Проверяем, файл ли это
                    .filter(path -> path.toString()
                            .toLowerCase(Locale.ROOT)
                            .endsWith(".mp3")).forEach(path -> { // Проверяем расширение файла
                        try {
                            formatter.format(path);
                        } catch (Mp3FileFormatException e) {
                            LOGGER.error(FILE_FORMATTING_ERROR_MSG, path.getFileName());
                            LOGGER.debug(e.toString());
                            Mp3FileFormatter.errorTracks.add(path);
                        } catch (InvalidAudioFrameException e) {
                            LOGGER.error(FILE_CORRUPTED_ERROR_MSG, path.getFileName());
                            LOGGER.debug(e.toString());
                            Mp3FileFormatter.errorTracks.add(path);
                        } catch (Exception e) {
                            LOGGER.error(UNKNOWN_ERROR_FORMATTING_FILE_MSG, path.getFileName());
                            LOGGER.debug(e.toString());
                            Mp3FileFormatter.errorTracks.add(path);
                        }
                    });
        } catch (IOException e) {
            LOGGER.error(FOLDER_READING_ERROR_MSG, SOURCE_PATH);
            LOGGER.debug(e.toString());
        }
    }

    /**
     * Метод перемещает все mp3 файлы из SOURCE_PATH в TARGET_PATH
     */
    private static void moveMp3Files() {
        try (Stream<Path> paths = Files.list(SOURCE_PATH)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(".mp3")) // Проверяем расширение файла
                    .forEach(path -> { // Проходимся по каждому mp3 файлу и перемещаем его
                        if (!Mp3FileFormatter.errorTracks.contains(path)) {
                            try {
                                // Перемещаем файл, если он не существует в целевой папке
                                if (Files.exists(TARGET_PATH.resolve(path.getFileName()))) {
                                    LOGGER.error(FILE_ALREADY_EXISTS_MSG, path.getFileName());
                                    Mp3FileFormatter.errorTracks.add(path);
                                } else {
                                    Files.move(path, TARGET_PATH.resolve(path.getFileName()));
                                }
                            } catch (IOException e) {
                                LOGGER.error(ERROR_MOVING_FILE_MSG, path.getFileName());
                                LOGGER.debug(e.toString());
                                Mp3FileFormatter.errorTracks.add(path);
                            }
                        }
                    });
        } catch (IOException e) {
            LOGGER.error(FOLDER_INTERACTION_ERROR_MSG, SOURCE_PATH);
            LOGGER.debug(e.toString());
        }
    }

    /**
     * Метод выводит логи о треках, которые были задействованы в форматировании
     */
    private static void printResults() {
        int countFiles = 0;
        try {
            // Выводим треки, которые удалось успешно отформатировать
            for (Path changedTrack : Mp3FileFormatter.changedTracks) {
                LOGGER.info("""

                        {}. "{}"
                        """, ++countFiles, changedTrack.getFileName());
            }
            countFiles = 0;
            // Выводим треки, при форматировании которых возникла ошибка
            for (Path errorTrack : Mp3FileFormatter.errorTracks) {
                LOGGER.error("""

                        {}. "{}"
                        """, ++countFiles, errorTrack.getFileName());
            }
            LOGGER.info("Треки с изменениями: {}", Mp3FileFormatter.changedTracks.size());
            LOGGER.info("Треки с ошибками: {}", Mp3FileFormatter.errorTracks.size());
        } catch (Exception e) {
            LOGGER.error("Произошла ошибка при выводе измененных треков");
            LOGGER.debug(e.toString());
        }
    }

    public static void run(boolean moveFiles) {
        formatFiles();
        if (moveFiles) {
            moveMp3Files();
        }
        printResults();
    }
}
