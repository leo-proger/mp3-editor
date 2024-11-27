package com.github.Leo_Proger.mp3_file_handlers;

import com.github.Leo_Proger.config.Config;
import com.github.Leo_Proger.exceptions.Mp3FileFormattingException;
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
     * List of modified files
     */
    private final List<Path> changedTracks = new LinkedList<>();

    /**
     * Files that could not be formatted.
     * They cannot be moved.
     */
    private final Map<Path, String> errorTracks = new HashMap<>();

    /**
     * The main method starts formatting, moving MP3 files and prints a summary
     *
     * @param moveFiles {@code true} - files are moved to target folder,
     *                  {@code false} - files aren't move to target folder
     */
    public void run(boolean moveFiles) {
        formatAndMoveFiles(moveFiles);
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
        for (Path changedTrack : changedTracks) {
            LOGGER.info("{}. \"{}\"",
                    ++countFiles, changedTrack.getFileName());
        }
        countFiles = 0;

        // Print files with errors
        for (Map.Entry<Path, String> entry : errorTracks.entrySet()) {
            Path errorTrack = entry.getKey();
            String errorMessage = entry.getValue();

            LOGGER.error("{}. {} - \"{}\"",
                    ++countFiles, errorMessage, errorTrack.getFileName());
        }
        LOGGER.info("Modified files: {}", changedTracks.size());
        LOGGER.info("Error files: {}", errorTracks.size());
    }

    /**
     * Format and move MP3 files from SOURCE_PATH to TARGET_PATH
     *
     * @param moveFiles {@code true} - files are moved to the target folder,
     *                  {@code false} - files aren't moved to the target folder
     * @see Config#SOURCE_PATH
     * @see Config#TARGET_PATH
     */
    private void formatAndMoveFiles(boolean moveFiles) {
        try (Stream<Path> paths = Files.list(SOURCE_PATH)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".mp3"))
                    .forEach(path -> processFile(path, moveFiles));
        } catch (IOException e) {
            LOGGER.debug("{}: {}\nPath: {}", e, UNABLE_TO_READ_FOLDER.getMessage(), SOURCE_PATH);
        }
    }

    /**
     * Process an MP3 file
     *
     * @param path      full path to file
     * @param moveFiles {@code true} - files are moved to the target folder,
     *                  {@code false} - files aren't moved to the target folder
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

            // Recheck that file isn't in errorTracks because moveFiles() could add it to that list
            if (!errorTracks.containsKey(newPath)) {
                changedTracks.add(newPath);
            }
        } catch (Exception e) {
            handleFileProcessingError(path, e);
        }
    }

    /**
     * Move file to specified folder
     *
     * @param fromFile full path to file to be moved
     * @param toDir    full path to folder to move file to
     */
    public void moveFile(Path fromFile, Path toDir) {
        try {
            // Move file if it doesn't exist in target folder
            Path targetPath = toDir.resolve(fromFile.getFileName());
            if (Files.exists(targetPath)) {
                errorTracks.put(fromFile, FILE_ALREADY_EXISTS.getMessage().formatted(toDir));
            } else {
                Files.move(fromFile, targetPath);
            }
        } catch (IOException e) {
            errorTracks.put(fromFile, UNABLE_TO_MOVE_FILE.getMessage());
        }
    }

    /**
     * Rename file
     *
     * @param fromName full path to current file
     * @param toName   full path to new file with new filename
     * @throws IOException errors when renaming file
     */
    public void renameFile(Path fromName, Path toName) throws IOException {
        Files.move(fromName, toName, StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * Handle errors that occur during file processing and add file to errorTracks
     *
     * @param path      path to file that error occurred in processing
     * @param exception exception that occurred during file processing
     * @see FileManager#errorTracks
     */
    private void handleFileProcessingError(Path path, Exception exception) {
        switch (exception) {
            case Mp3FileFormattingException e -> errorTracks.put(e.FILENAME, e.MESSAGE);
            case InvalidAudioFrameException ignored -> errorTracks.put(path, FILE_CORRUPTED.getMessage());
            case FileAlreadyExistsException ignored ->
                    errorTracks.put(path, FILE_ALREADY_EXISTS.getMessage().formatted(SOURCE_PATH));
            case FileSystemException ignored -> errorTracks.put(path, FILE_IN_USE_BY_ANOTHER_PROCESS.getMessage());
            case org.jaudiotagger.audio.exceptions.CannotWriteException ignored ->
                    errorTracks.put(path, FILE_ACCESS_DENIED.getMessage());
            case null, default -> {
                LOGGER.error("Unknown error: ", exception);
                errorTracks.put(path, UNKNOWN.getMessage());
            }
        }
    }
}
