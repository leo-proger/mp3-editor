package com.github.Leo_Proger.mp3_file_handlers;

import com.github.Leo_Proger.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;

import static com.github.Leo_Proger.config.Config.SOURCE_PATH;
import static com.github.Leo_Proger.config.Config.TARGET_PATH;

public class FileManager {
    private static final Logger log = LoggerFactory.getLogger(FileManager.class);

    /**
     * List of modified files
     */
    private final List<Path> modifiedFiles = new LinkedList<>();

    /**
     * Files that could not be formatted.
     * They cannot be moved.
     */
    private final Map<Path, String> errorFiles = new HashMap<>();

    /**
     * The main method starts formatting, moving MP3 files and prints a summary
     *
     * @param allowFileMove {@code true} - files will be moved to target dir,
     *                      {@code false} - files will not be moved to target dir
     */
    public void run(boolean allowFileMove) {
        formatAndMoveFiles(allowFileMove);
        printResults();
    }

    /**
     * Prints results:
     * <p>
     * 1. Modified files
     * <p>
     * 2. Files with errors
     * <p>
     * 3. Number of modified files
     * <p>
     * 4. Number of files with errors
     */
    private void printResults() {
        int countFiles = 0;

        // Print successfully formatted files
        for (Path file : modifiedFiles) {
            log.info("{}. \"{}\"", ++countFiles, file.getFileName());
        }
        countFiles = 0;

        // Sort error files by error message
        List<Map.Entry<Path, String>> sortedErrorEntries = new ArrayList<>(errorFiles.entrySet());
        sortedErrorEntries.sort(Map.Entry.comparingByValue());

        // Print files with errors
        for (Map.Entry<Path, String> entry : sortedErrorEntries) {
            Path errorFile = entry.getKey();
            String errorMessage = entry.getValue();

            log.error("{}. {} - {}", ++countFiles, errorFile.getFileName(), errorMessage);
        }
        log.info("Modified files: {}", modifiedFiles.size());
        log.info("Error files: {}", errorFiles.size());
    }

    /**
     * Format and move MP3 files from SOURCE_PATH to TARGET_PATH
     *
     * @param allowFileMove {@code true} - files will be moved to target dir,
     *                      {@code false} - files will not be moved to target dir
     * @see Config#SOURCE_PATH
     * @see Config#TARGET_PATH
     */
    private void formatAndMoveFiles(boolean allowFileMove) {
        try (Stream<Path> paths = Files.list(SOURCE_PATH)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".mp3"))
                    .forEach(path -> processFile(path, allowFileMove));
        } catch (IOException e) {
            log.error("Unable to read dir \"{}\"", SOURCE_PATH, e);
        }
    }

    /**
     * Process an MP3 file
     *
     * @param path          full path to file
     * @param allowFileMove {@code true} - files will be moved to target dir,
     *                      {@code false} - files will not be moved to target dir
     */
    private void processFile(Path path, boolean allowFileMove) {
        FileFormatter formatter = new FileFormatter();
        Path newPath;
        try {
            newPath = formatter.format(path);
            renameFile(path, newPath);

            if (allowFileMove && !errorFiles.containsKey(path)) {
                moveFile(newPath, TARGET_PATH);
            }

            // Recheck that file is not in errorTracks because allowFileMove() could add it to that list
            if (!errorFiles.containsKey(newPath)) {
                modifiedFiles.add(newPath);
            }
        } catch (Exception e) {
            String errorMessage = switch (e.getClass().getSimpleName()) {
                case "InvalidAudioFrameException" -> "File corrupted";
//                case "FileAlreadyExistsException" -> "File already exists in \"%s\"".formatted(TARGET_PATH);
                case "FileSystemException" -> "File in use by another process";
                case "CannotWriteException" -> "File access denied";
                default -> e.getMessage();
            };
            errorFiles.put(path, errorMessage);
            log.debug("Error while processing file \"{}\"", path, e);
        }
    }

    /**
     * Move file to specified dir
     *
     * @param file full path to file to be moved
     * @param dir  full path to dir to move file to
     */
    public void moveFile(Path file, Path dir) throws IOException {
        Path newFilePath = dir.resolve(file.getFileName());
        if (Files.exists(newFilePath)) {
            throw new FileAlreadyExistsException("File already exists in \"%s\"".formatted(dir));
        }
        Files.move(file, newFilePath);
    }

    /**
     * Rename file
     *
     * @param oldName full path to current file
     * @param newName full path to new file with new filename
     * @throws IOException errors when renaming file
     */
    public void renameFile(Path oldName, Path newName) throws IOException {
        if (!oldName.equals(newName)) {
            Files.move(oldName, newName, StandardCopyOption.ATOMIC_MOVE);
        }
    }
}
