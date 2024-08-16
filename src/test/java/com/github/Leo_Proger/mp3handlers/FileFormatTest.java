package com.github.Leo_Proger.mp3handlers;

import com.github.Leo_Proger.mp3_file_handlers.FileFormatter;
import com.github.Leo_Proger.mp3_file_handlers.FileManager;
import com.github.Leo_Proger.mp3_file_handlers.FilenameFormatter;
import com.github.Leo_Proger.mp3_file_handlers.Mp3FileFormattingException;
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
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

public class FileFormatTest {

    private static FileFormatter formatter;

    @TempDir
    Path tempDir;
    Path BASE_RESOURCES_PATH = Path.of("src/test/resources/com/github/Leo_Proger/");

    @BeforeEach
    public void setup() throws IOException {
        formatter = new FileFormatter();

        // Копируем все файлы из BASE_RESOURCES_PATH во временную директорию
        Files.list(BASE_RESOURCES_PATH).forEach(file -> {
            try {
                Files.copy(file, tempDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void testRenameFile() throws Mp3FileFormattingException, IOException, CannotWriteException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException {
        // Случай 1
        String original1 = "lxst cxntury  ,Цой x aboba feat aboab ft. name x sosy_jopy - Кончится Лето__--_-(remix-x.ru) [Music Video].mp3";

        String formatted1 = FilenameFormatter.run(original1);

        String expected1 = "LXST_CXNTURY, Цой, aboba, aboab, name, sosy_jopy_-_Кончится_Лето.mp3";
        assertEquals(formatted1, expected1);

        // Случай 2
        String original2 = "Смысловые Галлюцинации_-_Вечно молодой_(Phonk remix)_(official music video)--___(EEMUSIC.ru).mp3";

        String formatted2 = FilenameFormatter.run(original2);

        String expected2 = "Смысловые_Галлюцинации_-_Вечно_молодой_(Phonk_remix).mp3";
        assertEquals(formatted2, expected2);

        // Случай 3
        String original3 = "Jason Paris, Amøn - Heading North.mp3";

        String formatted3 = FilenameFormatter.run(original3);

        String expected3 = "Jason_Paris, Amon_-_Heading_North.mp3";
        assertEquals(formatted3, expected3);

        // Случай 4
        String[] strings = {"HXVRMXN.mp3", "HXVRMXN- .mp3", "HXVRMXN -j.mp3", "HXVRMXN-.mp3"};
        for (String string : strings) {
            assertThrows(Mp3FileFormattingException.class, () -> formatter.format(Path.of(string)));
        }
    }

    @Test
    public void testEditMetadata() throws InvalidDataException, UnsupportedTagException, IOException, CannotWriteException, CannotReadException, TagException, Mp3FileFormattingException, InvalidAudioFrameException, ReadOnlyFileException {
        // Форматируем файл
        Path original = tempDir.resolve("Øneheart, reidenshi - snowfall.mp3");

        Path formatted = formatter.format(original);

        FileManager.renameFile(original, formatted);

        // Проверяем форматирование метаданных
        Mp3File mp3FileObj = new Mp3File(tempDir.resolve(original.getParent().resolve(formatted.getFileName())));
        ID3v2 tag = mp3FileObj.getId3v2Tag();

        assertEquals(tag.getArtist(), "Oneheart; reidenshi");
        assertEquals(tag.getTitle(), "snowfall");
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
    public void testMoveFile() throws IOException {
        Path fromDir = tempDir.resolve("folder_from");
        Path toDir = tempDir.resolve("folder_to");

        // Создаем папки и перемещаемый файл
        Files.createDirectories(fromDir);
        Files.createDirectories(toDir);

        String file = "5admin_-_Silence.mp3";
        Files.createFile(fromDir.resolve(file));

        // Проверяем, что файл существует в folder_from
        assertTrue(Files.exists(fromDir.resolve(file)));

        FileManager.moveFile(fromDir.resolve(file), toDir);

        assertFalse(Files.exists(fromDir.resolve(file)));
        assertTrue(Files.exists(toDir.resolve(file)));
    }
}