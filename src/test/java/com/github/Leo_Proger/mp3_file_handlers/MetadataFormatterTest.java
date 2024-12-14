package com.github.Leo_Proger.mp3_file_handlers;

import com.github.Leo_Proger.config.Config;
import com.github.Leo_Proger.exceptions.Mp3FileFormattingException;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MetadataFormatterTest {
    private MetadataFormatter metadataFormatter;
    private Path exampleMp3File;
    private Path exampleMp3File2;

    final Path BASE_RESOURCES_PATH = Path.of("src/test/resources/com/github/Leo_Proger/");

    @BeforeEach
    void setUp() {
        metadataFormatter = new MetadataFormatter();
        exampleMp3File = BASE_RESOURCES_PATH.resolve("Oneheart, reidenshi_-_snowfall.mp3");
        exampleMp3File2 = BASE_RESOURCES_PATH.resolve("boneles_s, SVARDSTAL_-_No_Mercy.mp3");
    }

    @AfterEach
    void tearDown() throws CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException {
        resetMp3FileMetadata(exampleMp3File);
        resetMp3FileMetadata(exampleMp3File2);
    }

    @Test
    void testSuccessfulMetadataFormatting() throws Exception {
        // Run metadata formatting
        metadataFormatter.run(exampleMp3File, exampleMp3File.getFileName().toString());

        // Verify metadata
        AudioFile audioFile = AudioFileIO.read(exampleMp3File.toFile());
        Tag tag = audioFile.getTag();

        assertEquals("Oneheart" + Config.ARTISTS_DELIMITER_IN_METADATA.getDelimiter() + "reidenshi",
                tag.getFirst(FieldKey.ARTIST));
        assertEquals("snowfall", tag.getFirst(FieldKey.TITLE));
    }

    @Test
    void testInvalidFilenameFormat() {
        Path originalFile = Path.of("InvalidFilename.mp3");

        // Validate that an exception is thrown for invalid filename
        assertThrows(Mp3FileFormattingException.class, () ->
                metadataFormatter.run(originalFile, "InvalidFilename")
        );
    }

    @Test
    void testArtistsExclusion() throws Exception {
        metadataFormatter.run(exampleMp3File2, exampleMp3File2.getFileName().toString());

        AudioFile audioFile = AudioFileIO.read(exampleMp3File2.toFile());
        Tag tag = audioFile.getTag();

        assertEquals("boneles_s" + Config.ARTISTS_DELIMITER_IN_METADATA.getDelimiter() + "SVARDSTAL",
                tag.getFirst(FieldKey.ARTIST));
        assertEquals("No Mercy", tag.getFirst(FieldKey.TITLE));
    }

    @Test
    void testPreserveCoverArt() throws Exception {
        metadataFormatter.run(exampleMp3File2, exampleMp3File2.getFileName().toString());

        AudioFile updatedAudioFile = AudioFileIO.read(exampleMp3File2.toFile());

        // Verify cover art is preserved
        assertNotNull(updatedAudioFile.getTag().getFirstField(FieldKey.COVER_ART));
    }

    private void resetMp3FileMetadata(Path file) throws CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException {
        try {
            AudioFile audioFile = AudioFileIO.read(file.toFile());

            Tag tag = audioFile.getTag();

            tag.setField(FieldKey.ARTIST, "");
            tag.setField(FieldKey.ARTISTS, "");
            tag.setField(FieldKey.TITLE, "");
        } catch (IOException e) {
            throw new RuntimeException("Failed to reset MP3 file metadata", e);
        }
    }
}