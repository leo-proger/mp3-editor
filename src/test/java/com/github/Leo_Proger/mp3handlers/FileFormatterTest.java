package com.github.Leo_Proger.mp3handlers;

import com.github.Leo_Proger.mp3_editor.mp3_file_handlers.FileFormatter;
import com.github.Leo_Proger.mp3_editor.mp3_file_handlers.Mp3FileFormattingException;
import com.github.Leo_Proger.mp3_editor.mp3_file_handlers.FileManager;
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

public class FileFormatterTest {

    @TempDir
    Path tempDir;
    Path BASE_RESOURCES_PATH = Path.of("X:\\Programming\\java_projects\\mp3_editor\\src\\test\\resources\\com\\github\\Leo_Proger\\");

    private static FileFormatter formatter;

    @BeforeEach
    public void setup() {
        formatter = new FileFormatter();
    }

    @Test
    public void testRenameFile() throws Mp3FileFormattingException, IOException, CannotWriteException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException {
        // Случай 1
        Path originalFile1 = tempDir.resolve(BASE_RESOURCES_PATH.resolve("lxst cxntury  ,Цой x aboba feat aboab ft. name x sosy_jopy - Кончится Лето__--_-(remix-x.ru) [Music Video].mp3"));

        Path newFilename1 = formatter.format(originalFile1);

        Path expectedString1 = tempDir.resolve(BASE_RESOURCES_PATH.resolve("LXST_CXNTURY, Цой, aboba, aboab, name, sosy_jopy_-_Кончится_Лето.mp3"));
        assertEquals(newFilename1, expectedString1);

        // Случай 2
        Path originalFile2 = tempDir.resolve(BASE_RESOURCES_PATH.resolve("Смысловые Галлюцинации_-_Вечно молодой_(Phonk remix)_(official music video)--___(EEMUSIC.ru).mp3"));

        Path newFilename2 = formatter.format(originalFile2);

        Path expectedString2 = tempDir.resolve(BASE_RESOURCES_PATH.resolve("Смысловые_Галлюцинации_-_Вечно_молодой_(Phonk_remix).mp3"));
        assertEquals(newFilename2, expectedString2);

        // Случай 3
        Path originalFile3 = tempDir.resolve(BASE_RESOURCES_PATH.resolve("Jason Paris, Amøn - Heading North.mp3"));

        Path newFilename3 = formatter.format(originalFile3);

        Path expectedString3 = tempDir.resolve(BASE_RESOURCES_PATH.resolve("Jason_Paris, Amon_-_Heading_North.mp3"));
        assertEquals(newFilename3, expectedString3);

        // Случай 4
        String[] strings = {"HXVRMXN.mp3", "HXVRMXN- .mp3", "HXVRMXN -j.mp3", "HXVRMXN-.mp3"};
        for (String string : strings) {
            assertThrows(Mp3FileFormattingException.class, () -> formatter.format(Path.of(string)));
        }
    }

    @Test
    public void testEditMetadata() throws InvalidDataException, UnsupportedTagException, IOException, CannotWriteException, CannotReadException, TagException, Mp3FileFormattingException, InvalidAudioFrameException, ReadOnlyFileException {
        // Форматируем и переименовываем файл
        Path originalMp3File = Path.of(BASE_RESOURCES_PATH.toString(), "Øneheart, Reidenshi - snowfall.mp3");

        Path newMp3Filename = formatter.format(originalMp3File);
        FileManager.renameFile(originalMp3File, newMp3Filename);

        // Проверяем форматирование метаданных
        Mp3File mp3FileObj = new Mp3File(newMp3Filename);
        ID3v2 tag = mp3FileObj.getId3v2Tag();

        assertEquals(tag.getArtist(), "Oneheart; Reidenshi");
        assertEquals(tag.getTitle(), "snowfall");

        // Переименовываем обратно для последующих тестов
        Path expectedMp3File = Path.of(BASE_RESOURCES_PATH.toString(), "Oneheart, Reidenshi_-_snowfall.mp3");
        Files.move(expectedMp3File, originalMp3File);
    }

    @Test
    public void testRegularExpressionValidation() {
        String[] correctStrings = {
                "Валентин_Стрыкало, DJ_SESAME_-_Наше_лето_(Phonk_remix).mp3",
                "Zayn123_-_Dusk234_Till_Dawn2342.mp3",
                "Ya$h, SXNSTXRM, Kingpin_Skinny_Pimp_-_SAMURAI_PHONK.mp3",
                "X-WAYNE, SVFXNXV, CURSEDEVIL, WESTLIBERTY'S, 74blade, LEYNCLOUD, BXGR, ARGXNTUM, SH3TLVIZ, KALXSH, ROXSH_LUXIRY, cxsredead, DJ_CHANSEY, THRILLMANE, LXSTPLVYER, NESMIYANOV_-_WORLDWIDE.mp3",
                "VERV!X_-_Goodbye_Vol._3.mp3",
                "TRVNSPORTER, G.P.R_Beat_-_Champion.mp3",
                "Kungs, Cookin'_On_3_Burners_-_This_Girl.mp3",
                "VØJ, ATSMXN_-_Criminal_Breath.mp3",
                "KIM, Øneheart_-_NIGHTEXPRESS.mp3",
                "_zodivk, Bearded_Legend__-_The_Wayfarer_.mp3",
        };
        for (String string : correctStrings) {
            assertTrue(FileFormatter.isValidMp3Filename(string));
        }

        String[] incorrectStrings = {
                "X:\\Folder\\Би-2_-_Полковнику_никто_не_пишет.mp3",
                "zodivk, Bearded_Legend_-_The_Wayfarer",
                "zodivk,Bearded_Legend_-_The_Wayfarer",
                " zodivk, Bearded_Legend_-_The_Wayfarer.mp3",
                "zodivk, Bearded_Legend _-_The_Wayfarer.mp3",
        };
        for (String string : incorrectStrings) {
            assertFalse(FileFormatter.isValidMp3Filename(string));
        }
    }

    @Test
    public void testMoveFile() {
        Path fromDir = Path.of("X:\\Programming\\java_projects\\mp3_editor\\src\\test\\resources\\com\\github\\Leo_Proger\\folder_from");
        Path toDir = Path.of("X:\\Programming\\java_projects\\mp3_editor\\src\\test\\resources\\com\\github\\Leo_Proger\\folder_to");
        String file = "5admin_-_Silence.mp3";

        // Проверяем, что файл существует в folder_from
        assertTrue(Files.exists(fromDir.resolve(file)));

        FileManager.moveFile(fromDir.resolve(file), toDir);

        assertFalse(Files.exists(fromDir.resolve(file)));
        assertTrue(Files.exists(toDir.resolve(file)));

        // Перемещаем обратно для последующих тестов
        FileManager.moveFile(toDir.resolve(file), fromDir);
    }
}