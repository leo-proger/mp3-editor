package com.github.Leo_Proger.mp3_editor.mp3handlers;

import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.Leo_Proger.mp3_editor.main.Config.SOURCE_PATH;
import static com.github.Leo_Proger.mp3_editor.main.Config.TARGET_PATH;
import static com.github.Leo_Proger.mp3_editor.main.ErrorMessage.*;

public class Mp3FileManager {
    static {
        LOGGER = LoggerFactory.getLogger(Mp3FileFormatter.class);
    }
    private static final Logger LOGGER;

    /**
     * Список измененных треков, чтобы в конце программы вывести сводку
     */
    public final static List<Path> changedTracks = new LinkedList<>();

    /**
     * Треки с их ошибками, при форматировании которых выдало ошибку, и их не нужно перемещать
     */
    public final static Map<Path, String> errorTracks = new HashMap<>();

    /**
     * Метод форматирует и сразу же перемещает каждый mp3 файл в целевую папку
     */
    private static void formatAndMoveFiles(Path fromDir, Path toDir, boolean moveFiles) {
        try (Stream<Path> paths = Files.list(fromDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString()
                            .toLowerCase()
                            .endsWith(".mp3"))
                    .forEach(path -> {
                        // Форматируем файла
                        Mp3FileFormatter formatter = new Mp3FileFormatter();
                        Path newPath = path;
                        try {
                            // Получаем путь к файлу с отформатированным именем
                            newPath = formatter.format(path);

                            // Переименовываем файл
                            renameFile(path, newPath);
                        } catch (Mp3FileFormattingException e) {
                            errorTracks.put(e.FILENAME, e.MESSAGE);
                        } catch (InvalidAudioFrameException e) {
                            errorTracks.put(path, FILE_CORRUPTED_ERROR.getMessage());
                        } catch (FileAlreadyExistsException e) {
                            errorTracks.put(path, FILE_ALREADY_EXISTS_ERROR.getMessage().formatted(fromDir));
                        } catch (Exception e) {
                            errorTracks.put(path, UNKNOWN_ERROR_FORMATTING_FILE.getMessage());
                        }
                        // Перемещаем файл, если moveFiles == true, и он не находится в errorTracks
                        if (moveFiles && !errorTracks.containsKey(path)) {
                            moveFile(newPath, toDir);
                        }
                        // Перепроверяем, что файл не находится в errorTracks, и добавляем в changedTracks
                        if (!errorTracks.containsKey(path)) {
                            changedTracks.add(newPath);
                        }
                    });
        } catch (IOException e) {
            LOGGER.debug("{}: {}\nПуть: {}", e, FOLDER_READING_ERROR.getMessage(), fromDir);
        }
    }

    public static void moveFile(Path fromFile, Path toDir) {
        try {
            // Перемещаем файл, если он не существует в целевой папке
            if (Files.exists(toDir.resolve(fromFile.getFileName()))) {
                errorTracks.put(fromFile, FILE_ALREADY_EXISTS_ERROR.getMessage().formatted(toDir));
            } else {
                Files.move(fromFile, toDir.resolve(fromFile.getFileName()));
            }
        } catch (IOException e) {
            errorTracks.put(fromFile, ERROR_MOVING_FILE.getMessage());
        }
    }

    public static void renameFile(Path from, Path to) throws IOException {
        Files.move(from, to, StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * Метод выводит логи о треках, которые были задействованы в форматировании
     */
    private static void printResults() {
        int countFiles = 0;

        // Выводим треки, которые удалось успешно отформатировали
        for (Path changedTrack : changedTracks) {
            LOGGER.info("{}. \"{}\"",
                    ++countFiles, changedTrack.getFileName());
        }
        countFiles = 0;

        // Выводим треки, при форматировании которых возникла ошибка
        for (Map.Entry<Path, String> entry : errorTracks.entrySet()) {
            Path errorTrack = entry.getKey();
            String errorMessage = entry.getValue();

            LOGGER.error("{}. {} - \"{}\"",
                    ++countFiles, errorMessage, errorTrack.getFileName());
        }
        LOGGER.info("Треки с изменениями: {}", changedTracks.size());
        LOGGER.info("Треки с ошибками: {}", errorTracks.size());
    }

    public static void run(boolean moveFiles) {
        formatAndMoveFiles(SOURCE_PATH, TARGET_PATH, moveFiles);
        printResults();
    }
}
