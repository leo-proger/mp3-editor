package mp3handlers;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Mp3FormatterTest extends Mp3Formatter {

    @Test
    public void testRemoveAd() throws Mp3FormatException {
        assertEquals(
                Mp3Formatter.removeAd("WXXLER_-_Demons_In_My_Blood_(Phonk_remix)_(ru.soundmax.me).mp3"),
                "WXXLER_-_Demons_In_My_Blood_(Phonk_remix).mp3"
        );
        assertEquals(
                Mp3Formatter.removeAd("Смысловые_Галлюцинации_-_Вечно_молодой_(Phonk_remix)(axemusic.ru).mp3"),
                "Смысловые_Галлюцинации_-_Вечно_молодой_(Phonk_remix).mp3"
        );
        assertEquals(
                Mp3Formatter.removeAd("zodivk, MVKO, ROCSET_MOON_-_WITCHER___-__-(remix-x.ru).mp3"),
                "zodivk, MVKO, ROCSET_MOON_-_WITCHER.mp3"
        );
        assertEquals(
                Mp3Formatter.removeAd("zecki, Phonkha_-_SLAUGHTER_HOUSE_2_(EEMUSIC.ru).mp3"),
                "zecki, Phonkha_-_SLAUGHTER_HOUSE_2.mp3"
        );
        assertThrows(Mp3FormatException.class, () -> Mp3Formatter.removeAd("HXVRMXN_-_スピードデーモン.mp3"));
    }

    @Test
    public void testFormatMp3Filename() {
        // TODO: Написать тесты
    }

    @Test
    public void testFormatMetadata() throws Mp3FormatException, IOException, CannotWriteException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException {
        try {
            File file = new File("X:\\Music\\HXVRMXN_-_スピードデーモン.mp3");
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            Mp3Formatter.formatMetadata(file.getAbsolutePath());

            assertEquals(
                    tag.getFirst(FieldKey.ARTIST), "HXVRMXN"
            );
            assertEquals(
                    tag.getFirst(FieldKey.TITLE), "スピードデーモン"
            );
        } catch (FileNotFoundException e) {
            System.err.println("Файл не найден");
        }

        try {
            File file = new File("X:\\Music\\HXVRMXN, Shawty_Pimp, MC_Spade_-_SUPERIOR.mp3");
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            Mp3Formatter.formatMetadata(file.getAbsolutePath());

            assertEquals(
                    tag.getFirst(FieldKey.ARTIST), "HXVRMXN; Shawty Pimp; MC Spade"
            );
            assertEquals(
                    tag.getFirst(FieldKey.TITLE), "SUPERIOR"
            );
        } catch (FileNotFoundException e) {
            System.err.println("Файл не найден");
        }

        try {
            File file = new File("X:\\Music\\HXRIZXN, LOMQ_-_\uD835\uDC77\uD835\uDC6F\uD835\uDC76\uD835\uDC75\uD835\uDC72.mp3");
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            Mp3Formatter.formatMetadata(file.getAbsolutePath());

            assertEquals(
                    tag.getFirst(FieldKey.ARTIST), "HXRIZXN; LOMQ"
            );
            assertEquals(
                    tag.getFirst(FieldKey.TITLE), "\uD835\uDC77\uD835\uDC6F\uD835\uDC76\uD835\uDC75\uD835\uDC72"
            );
        } catch (FileNotFoundException e) {
            System.err.println("Файл не найден");
        }

        try {
            File file = new File("X:\\Music\\HXELLPLAYA, Nissan_Playa, 4WHEEL, DiouxxieRounds_-_ICREDIBLE_DAMAGE.mp3");
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            Mp3Formatter.formatMetadata(file.getAbsolutePath());

            assertEquals(
                    tag.getFirst(FieldKey.ARTIST), "HXELLPLAYA; Nissan Playa; 4WHEEL; DiouxxieRounds"
            );
            assertEquals(
                    tag.getFirst(FieldKey.TITLE), "ICREDIBLE DAMAGE"
            );
        } catch (FileNotFoundException e) {
            System.err.println("Файл не найден");
        }

        try {
            File file = new File("X:\\Music\\BONES_-_ChangeOfScenery_(Skeler_Remix).mp3");
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            Mp3Formatter.formatMetadata(file.getAbsolutePath());

            assertEquals(
                    tag.getFirst(FieldKey.ARTIST), "BONES"
            );
            assertEquals(
                    tag.getFirst(FieldKey.TITLE), "ChangeOfScenery (Skeler Remix)"
            );
        } catch (FileNotFoundException e) {
            System.err.println("Файл не найден");
        }

        try {
            File file = new File("X:\\Music\\boneles_s, SVARDSTAL_-_No_Mercy.mp3");
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            Mp3Formatter.formatMetadata(file.getAbsolutePath());

            assertEquals(
                    tag.getFirst(FieldKey.ARTIST), "boneles_s; SVARDSTAL"
            );
            assertEquals(
                    tag.getFirst(FieldKey.TITLE), "No Mercy"
            );
        } catch (FileNotFoundException e) {
            System.err.println("Файл не найден");
        }

        try {
            File file = new File("X:\\Music\\BLESSED_MANE_-_D.K_3.mp3");
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            Mp3Formatter.formatMetadata(file.getAbsolutePath());

            assertEquals(
                    tag.getFirst(FieldKey.ARTIST), "BLESSED MANE"
            );
            assertEquals(
                    tag.getFirst(FieldKey.TITLE), "D.K 3"
            );
        } catch (FileNotFoundException e) {
            System.err.println("Файл не найден");
        }

        try {
            File file = new File("X:\\Music\\Big_Baby_Tape, Дора_-_Gimme_The_Дура.mp3");
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            Mp3Formatter.formatMetadata(file.getAbsolutePath());

            assertEquals(
                    tag.getFirst(FieldKey.ARTIST), "Big Baby Tape; Дора"
            );
            assertEquals(
                    tag.getFirst(FieldKey.TITLE), "Gimme The Дура"
            );
        } catch (FileNotFoundException e) {
            System.err.println("Файл не найден");
        }

        try {
            File file = new File("X:\\Music\\BADTRIP_MUSIC, GREEN_ORXNGE, PRXSXNT_FXTURE, 5admin, Hnar, Phonk_Killer, PHOROMANE, Gxxrx_Okxmi, Muscay_3, DJ_HOOPTIVILE_-_DRUG_ADDICTED.mp3");
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            Mp3Formatter.formatMetadata(file.getAbsolutePath());

            assertEquals(
                    tag.getFirst(FieldKey.ARTIST), "BADTRIP MUSIC; GREEN ORXNGE; PRXSXNT FXTURE; 5admin; Hnar; Phonk Killer; PHOROMANE; Gxxrx Okxmi; Muscay 3; DJ HOOPTIVILE"
            );
            assertEquals(
                    tag.getFirst(FieldKey.TITLE), "DRUG ADDICTED"
            );
        } catch (FileNotFoundException e) {
            System.err.println("Файл не найден");
        }

        try {
            File file = new File("X:\\Music\\B1ZARD_-_COMBO!.mp3");
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            Mp3Formatter.formatMetadata(file.getAbsolutePath());

            assertEquals(
                    tag.getFirst(FieldKey.ARTIST), "B1ZARD"
            );
            assertEquals(
                    tag.getFirst(FieldKey.TITLE), "COMBO!"
            );
        } catch (FileNotFoundException e) {
            System.err.println("Файл не найден");
        }
    }
}