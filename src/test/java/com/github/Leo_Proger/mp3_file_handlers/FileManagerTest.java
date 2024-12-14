package com.github.Leo_Proger.mp3_file_handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void renameFile_successfulRename() throws IOException {
        // Create temporary dirs for the test
        Path fromDir = tempDir.resolve("original_dir");
        Files.createDirectories(fromDir);

        // Prepare file names
        String originalFilename = "old_track.mp3";
        String newFilename = "new_track.mp3";
        Path originalFile = fromDir.resolve(originalFilename);
        Path newFile = fromDir.resolve(newFilename);

        // Create a source file
        Files.createFile(originalFile);
        // Write some content to ensure file is not empty
        Files.writeString(originalFile, "Test content");

        // Check the existence of the source file
        assertTrue(Files.exists(originalFile), "Original file should exist before renaming");

        // Perform renaming
        FileManager fileManager = new FileManager();
        fileManager.renameFile(originalFile, newFile);

        // Checking the result
        assertFalse(Files.exists(originalFile), "Original file should be deleted after renaming");
        assertTrue(Files.exists(newFile), "New file should be created after renaming");
        assertEquals(newFilename, newFile.getFileName().toString(), "File name should be changed");
        assertEquals("Test content", Files.readString(newFile), "File content should be preserved");
    }

    @Test
    void renameFile_doesNotThrowExceptionWhenFileAlreadyExists() throws IOException {
        // Create a temporary dir
        Path testDir = tempDir.resolve("rename_test_dir");
        Files.createDirectories(testDir);

        // Create two files
        Path sourceFile = testDir.resolve("source.mp3");
        Path targetFile = testDir.resolve("target.mp3");

        Files.createFile(sourceFile);
        Files.createFile(targetFile);

        // Create a FileManager
        FileManager fileManager = new FileManager();

        // Checking that an attempt to rename an existing file does not throw an exception
        assertDoesNotThrow(
                () -> fileManager.renameFile(sourceFile, targetFile),
                "Renaming to an existing file should not throw an exception"
        );
    }

    @Test
    void moveFile_completeWorkflow() throws IOException {
        // Create temporary dirs
        Path sourceDir = tempDir.resolve("source_music");
        Path targetDir = tempDir.resolve("target_music");
        Files.createDirectories(sourceDir);
        Files.createDirectories(targetDir);

        // Create test files with content
        String[] testFiles = {
                "track1.mp3",
                "track2.mp3",
                "track3.mp3"
        };

        // Collection for tracking moved files
        List<Path> movedFiles = new ArrayList<>();

        // Create files and move them
        FileManager fileManager = new FileManager();
        for (String fileName : testFiles) {
            Path sourceFile = sourceDir.resolve(fileName);

            // Create a file with unique content
            Files.writeString(sourceFile, "Content of " + fileName);

            // Move the file
            fileManager.moveFile(sourceFile, targetDir);

            // Track moved files
            movedFiles.add(targetDir.resolve(fileName));
        }

        // Main checks
        for (Path movedFile : movedFiles) {
            // Check that the file exists in the target dir
            assertTrue(Files.exists(movedFile),
                    "File should exist in target directory: " + movedFile.getFileName());

            // Check the contents of the file
            assertNotNull(Files.readString(movedFile),
                    "File content should be preserved: " + movedFile.getFileName());
        }

        // Additional checks
        assertEquals(testFiles.length, movedFiles.size(),
                "Number of moved files should match");

        // Check that the files no longer exist in the original directory
        for (String fileName : testFiles) {
            assertFalse(Files.exists(sourceDir.resolve(fileName)),
                    "Original file should be removed: " + fileName);
        }
    }
}