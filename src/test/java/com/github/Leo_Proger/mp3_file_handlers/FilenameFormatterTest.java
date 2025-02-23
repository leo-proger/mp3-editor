package com.github.Leo_Proger.mp3_file_handlers;

import com.github.Leo_Proger.exceptions.Mp3FileFormattingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilenameFormatterTest {
    private FilenameFormatter filenameFormatter;

    @BeforeEach
    void setUp() {
        filenameFormatter = new FilenameFormatter();
    }

    @Test
    void testSuccessfulFilenameFormatting() throws Mp3FileFormattingException {
        // Test case 1: Complex filename with multiple artists and BLACKLIST data
        String original1 = "lxst cxntury  ,Цой x aboba feat aboab ft. name x sosy_jopy - Кончится Лето__--_-(remix-x.ru) [Music Video].mp3";
        String expected1 = "LXST_CXNTURY, Цой, aboba, aboab, name, sosy_jopy_-_Кончится_Лето.mp3";
        assertEquals(expected1, filenameFormatter.run(original1));

        // Test case 2: Filename with BLACKLIST data
        String original2 = "Смысловые Галлюцинации_-_Вечно молодой_(Phonk remix)_(official music video)--___(EEMUSIC.ru).mp3";
        String expected2 = "Смысловые_Галлюцинации_-_Вечно_молодой_(Phonk_remix).mp3";
        assertEquals(expected2, filenameFormatter.run(original2));
    }

    @Test
    void testInvalidFilenameFormats() {
        String[] invalidFilenames = {
                "Artist Name.mp3",
                "Artist Name- .mp3",
                "Artist Name-.mp3",
                "Artist Name -Track Name",
                "Artist Name- Track_Name",
                "Artist Name-Track_Name",
                "Artist Name - Track_Name",
        };

        for (String invalidFilename : invalidFilenames) {
            assertThrows(Mp3FileFormattingException.class,
                    () -> filenameFormatter.run(invalidFilename),
                    "Should throw exception for invalid filename: " + invalidFilename
            );
        }
    }

    @Test
    void testNewArtistsCollection() throws Mp3FileFormattingException {
        // Reset the newArtists set before the test
        FilenameFormatter.getNewArtists().clear();

        String filename = "NewArtist1, NewArtist2_-_Some_Song.mp3";
        filenameFormatter.run(filename);

        Set<String> newArtists = FilenameFormatter.getNewArtists();
        assertEquals(2, newArtists.size());
        assertTrue(newArtists.contains("NewArtist1"));
        assertTrue(newArtists.contains("NewArtist2"));
    }

    @Test
    void testArtistSeparators() throws Mp3FileFormattingException {
        String[] testCases = {
                "Artist1 x Artist2_-_Song.mp3",
                "Artist1 feat Artist2_-_Song.mp3",
                "Artist1 ft. Artist2_-_Song.mp3",
                "Artist1 & Artist2_-_Song.mp3"
        };

        for (String testCase : testCases) {
            String formatted = filenameFormatter.run(testCase);
            assertEquals("Artist1, Artist2_-_Song.mp3",
                    formatted,
                    "Failed to correctly separate artists in: " + testCase);
        }
    }

    @Test
    void testCharacterReplacement() throws Mp3FileFormattingException {
        String original = "Arist Ø name with special ø characters’ ★ – Song.mp3";
        String formatted = filenameFormatter.run(original);

        // Verify that special characters have been replaced
        assertFalse(formatted.contains("’"));
        assertFalse(formatted.contains("–"));
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
                "_zodivk, Bearded_Legend__-_The_Wayfarer_.mp3",
                "KIM, Øneheart_-_NIGHTEXPRESS.mp3",
                "VØJ, ATSMXN_-_Criminal_Breath.mp3"
        };
        for (String string : correctStrings) {
            assertTrue(FileFormatter.isValidMp3Filename(string));
        }

        String[] incorrectStrings = {
                "X:\\Directory\\Би-2_-_Полковнику_никто_не_пишет.mp3",
                "zodivk, Bearded_Legend_-_The_Wayfarer",
                "zodivk,Bearded_Legend_-_The_Wayfarer.mp3",
                " zodivk, Bearded_Legend_-_The_Wayfarer.mp3",
                "zodivk, Bearded_Legend _-_The Wayfarer.mp3",
                "zodivk, Bearded_Legend_-_ The_Wayfarer.mp3",
                "zodivk, Bearded Legend_-_The_Wayfarer.mp3",
        };
        for (String string : incorrectStrings) {
            assertFalse(FileFormatter.isValidMp3Filename(string));
        }
    }
}