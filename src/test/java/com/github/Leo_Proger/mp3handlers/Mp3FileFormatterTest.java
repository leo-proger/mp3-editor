package com.github.Leo_Proger.mp3handlers;

import com.github.Leo_Proger.mp3_editor.mp3handlers.Mp3FileFormatException;
import com.github.Leo_Proger.mp3_editor.mp3handlers.Mp3FileFormatter;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Mp3FileFormatterTest {

    @TempDir
    Path tempDir;
    Path BASE_RESOURCES_PATH = Path.of("X:\\Programming\\java_projects\\mp3_editor\\src\\test\\resources\\com\\github\\Leo_Proger\\");

    private static Mp3FileFormatter formatter;

    @BeforeEach
    public void setup() {
        formatter = new Mp3FileFormatter();
    }

    @Test
    public void testRenameFile() throws Mp3FileFormatException, IOException, CannotWriteException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException {
        // Тест 1
        Path originalFile1 = tempDir.resolve(BASE_RESOURCES_PATH.resolve("lxst cxntury, Цой - Кончится Лето__--_-(remix-x.ru).mp3"));

        formatter.format(originalFile1);

        Path newFile1 = tempDir.resolve(BASE_RESOURCES_PATH.resolve("LXST_CXNTURY, Цой_-_Кончится_Лето.mp3"));
        assertTrue(Files.exists(newFile1));
        assertFalse(Files.exists(originalFile1));

        // Тест 2
        Path originalFile2 = tempDir.resolve(BASE_RESOURCES_PATH.resolve("Смысловые Галлюцинации_-_Вечно молодой_(Phonk remix)_(official music video).mp3"));

        formatter.format(originalFile2);

        Path newFile2 = tempDir.resolve(BASE_RESOURCES_PATH.resolve("Смысловые_Галлюцинации_-_Вечно_молодой_(Phonk_remix).mp3"));
        assertTrue(Files.exists(newFile2));
        assertFalse(Files.exists(originalFile2));

        // Переименовываем обратно для последующих тестов
        Files.move(newFile1, originalFile1);
        Files.move(newFile2, originalFile2);

        // Тест 3
        Path filename = Path.of("HXVRMXN.mp3");
        assertThrows(Mp3FileFormatException.class, () -> formatter.format(filename));

        // Тест 4
        Path filename2 = Path.of("HXVRMXN- .mp3");
        assertThrows(Mp3FileFormatException.class, () -> formatter.format(filename2));

        // Тест 5
        Path filename3 = Path.of("HXVRMXN -j.mp3");
        assertThrows(Mp3FileFormatException.class, () -> formatter.format(filename3));

        // Тест 6
        Path filename4 = Path.of("HXVRMXN-.mp3");
        assertThrows(Mp3FileFormatException.class, () -> formatter.format(filename4));
    }

    @Test
    public void testEditMetadata() throws InvalidDataException, UnsupportedTagException, IOException, CannotWriteException, CannotReadException, TagException, Mp3FileFormatException, InvalidAudioFrameException, ReadOnlyFileException {
        Path mp3File = Path.of(BASE_RESOURCES_PATH.toString(), "ЛЮБЭ, 37R - Давай за жизнь (Phonk Remix).mp3");
        Path newMp3File = Path.of(BASE_RESOURCES_PATH.toString(), "ЛЮБЭ, 37R_-_Давай_за_жизнь_(Phonk_Remix).mp3");

        Mp3File editedMp3File = new Mp3File(mp3File);
        ID3v2 tag = editedMp3File.getId3v2Tag();

        formatter.format(mp3File);

        assertEquals(tag.getArtist(), "ЛЮБЭ; 37R");
        assertEquals(tag.getTitle(), "Давай за жизнь (Phonk Remix)");

        // Переименовываем обратно для последующих тестов
        Files.move(newMp3File, mp3File);
    }
}