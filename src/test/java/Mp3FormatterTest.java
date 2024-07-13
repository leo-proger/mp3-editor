import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class Mp3FormatterTest {

    @Test
    public void removeAd() throws Mp3FormatException {
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
    public void formatMp3Filename() {
        // TODO: Написать тесты
    }
}