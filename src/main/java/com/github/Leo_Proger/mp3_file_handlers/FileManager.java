package com.github.Leo_Proger.mp3_file_handlers;

import com.github.Leo_Proger.config.Config;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.Leo_Proger.config.Config.SOURCE_PATH;
import static com.github.Leo_Proger.config.Config.TARGET_PATH;
import static com.github.Leo_Proger.config.ErrorMessage.*;

public class FileManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileManager.class);

    /**
     * Список измененных файлов.
     * Печатается в конце программы.
     */
    public final List<Path> changedTracks = new LinkedList<>();

    /**
     * Файлы, которые не удалось отформатировать.
     * Они не перемещаются.
     */
    public final Map<Path, String> errorTracks = new HashMap<>();

    /**
     * Главный метод, запускающий форматирование, перемещение MP3 файлов и печатает сводку.
     *
     * @param moveFiles {@code true} - файлы перемещаются в целевую папку.
     *                  {@code false} - файлы не перемещаются в целевую папку
     */
    public void run(boolean moveFiles) {
        formatAndMoveFiles(moveFiles);
        printResults();
    }

    /**
     * Выводит результаты работы программы:
     * <p>
     * 1. Имена измененных файлов
     * <p>
     * 2. Имена файлов с ошибками
     * <p>
     * 3. Количество измененных файлов
     * <p>
     * 4. Количество файлов с ошибками
     */
    private void printResults() {
        int countFiles = 0;

        // Выводит файлы, которые успешно отформатировало
        for (Path changedTrack : changedTracks) {
            LOGGER.info("{}. \"{}\"",
                    ++countFiles, changedTrack.getFileName());
        }
        countFiles = 0;

        // Выводит файлы, при форматировании которых возникла ошибка
        for (Map.Entry<Path, String> entry : errorTracks.entrySet()) {
            Path errorTrack = entry.getKey();
            String errorMessage = entry.getValue();

            LOGGER.error("{}. {} - \"{}\"",
                    ++countFiles, errorMessage, errorTrack.getFileName());
        }
        LOGGER.info("Треки с изменениями: {}", changedTracks.size());
        LOGGER.info("Треки с ошибками: {}", errorTracks.size());
    }

    /**
     * Форматирует и перемещает MP3 файлы из SOURCE_PATH в TARGET_PATH.
     *
     * @param moveFiles {@code true} - файлы перемещаются в целевую папку,
     *                  {@code false} - файлы не перемещаются в целевую папку
     * @see Config#SOURCE_PATH
     * @see Config#TARGET_PATH
     */
    private void formatAndMoveFiles(boolean moveFiles) {
        try (Stream<Path> paths = Files.list(SOURCE_PATH)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".mp3"))
                    .forEach(path -> processFile(path, moveFiles));
        } catch (IOException e) {
            LOGGER.debug("{}: {}\nПуть: {}", e, FOLDER_READING_ERROR.getMessage(), SOURCE_PATH);
        }
    }

    /**
     * Обрабатывает отдельный MP3 файл.
     *
     * @param path      путь к файлу
     * @param moveFiles {@code true} - файл перемещается в целевую папку,
     *                  {@code false} - файл не перемещается в целевую папку
     */
    private void processFile(Path path, boolean moveFiles) {
        FileFormatter formatter = new FileFormatter();
        Path newPath;
        try {
            newPath = formatter.format(path);
            renameFile(path, newPath);

            if (moveFiles && !errorTracks.containsKey(path)) {
                moveFile(newPath, TARGET_PATH);
            }
            // Перепроверяет, что файл не находится в errorTracks, потому что moveFiles() мог добавить его в этот список
            if (!errorTracks.containsKey(newPath)) {
                changedTracks.add(newPath);
            }
        } catch (Exception e) {
            handleFileProcessingError(path, e);
        }
    }

    /**
     * Перемещает файл в указанную директорию.
     *
     * @param fromFile путь к файлу, который нужно переместить
     * @param toDir    путь к директории, в которую нужно переместить
     */
    public void moveFile(Path fromFile, Path toDir) {
        try {
            // Перемещает файл, если он не существует в целевой папке
            Path targetPath = toDir.resolve(fromFile.getFileName());
            if (Files.exists(targetPath)) {
                errorTracks.put(fromFile, FILE_ALREADY_EXISTS_ERROR.getMessage().formatted(toDir));
            } else {
                Files.move(fromFile, targetPath);
            }
        } catch (IOException e) {
            errorTracks.put(fromFile, ERROR_MOVING_FILE.getMessage());
        }
    }

    /**
     * Переименовывает файл.
     *
     * @param fromName текущий путь файла
     * @param toName   новый путь файла
     * @throws IOException ошибки при переименовывании файла
     */
    public void renameFile(Path fromName, Path toName) throws IOException {
        Files.move(fromName, toName, StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * Обрабатывает ошибки, возникающие при обработке файла, и добавляет файл в errorTracks.
     *
     * @param path      путь к файлу, при обработке которого возникла ошибка
     * @param exception исключение, возникшее при обработке файла
     * @see FileManager#errorTracks
     */
    private void handleFileProcessingError(Path path, Exception exception) {
        switch (exception) {
            case Mp3FileFormattingException e -> errorTracks.put(e.FILENAME, e.MESSAGE);
            case InvalidAudioFrameException ignored -> errorTracks.put(path, FILE_CORRUPTED_ERROR.getMessage());
            case FileAlreadyExistsException ignored ->
                    errorTracks.put(path, FILE_ALREADY_EXISTS_ERROR.getMessage().formatted(SOURCE_PATH));
            case FileSystemException ignored ->
                    errorTracks.put(path, FILE_IN_USE_BY_ANOTHER_PROCESS_ERROR.getMessage());
            case org.jaudiotagger.audio.exceptions.CannotWriteException ignored ->
                    errorTracks.put(path, FILE_ACCESS_RESTRICTED.getMessage());
            case null, default -> {
                LOGGER.error("Неизвестная ошибка: ", exception);
                errorTracks.put(path, UNKNOWN_ERROR_FORMATTING_FILE.getMessage());
            }
        }
    }
}
