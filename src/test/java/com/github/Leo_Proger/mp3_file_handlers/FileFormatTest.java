package com.github.Leo_Proger.mp3_file_handlers;

import com.github.Leo_Proger.exceptions.Mp3FileFormattingException;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileFormatTest {

    private static FileFormatter formatter;
    private static Logger log;

    @TempDir
    Path tempDir;
    final Path BASE_RESOURCES_PATH = Path.of("src/test/resources/com/github/Leo_Proger/");

    @BeforeEach
    public void setup() throws IOException {
        log = LoggerFactory.getLogger(FileFormatTest.class);

        formatter = new FileFormatter();

        // Copy all files from BASE_RESOURCES_PATH to the temporary dir
        try (var files = Files.list(BASE_RESOURCES_PATH)) {
            files.forEach(file -> {
                try {
                    Files.copy(file, tempDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            });
        }
    }

    @Test
    public void testEditMetadata() throws
            InvalidDataException, UnsupportedTagException, IOException, CannotWriteException, CannotReadException, TagException, Mp3FileFormattingException, InvalidAudioFrameException, ReadOnlyFileException {
        // Format the file
        Path original = tempDir.resolve("Øneheart, reidenshi - snowfall.mp3");

        Path formatted = formatter.format(original);

        FileManager fileManager = new FileManager();
        fileManager.renameFile(original, formatted);

        // Check metadata formatting
        Mp3File mp3FileObj = new Mp3File(tempDir.resolve(original.getParent().resolve(formatted.getFileName())));
        ID3v2 tag = mp3FileObj.getId3v2Tag();

        assertEquals("Oneheart; reidenshi", tag.getArtist());
        assertEquals("snowfall", tag.getTitle());
    }
}