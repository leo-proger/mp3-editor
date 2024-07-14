package mp3handlers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Mp3FileFormatterTest {
    // TODO: Убрать логи, которые пишет jaudiotagger

//    private static Mp3FileFormatter formatter;
//    private static final String defaultParentPath = "X:\\Music\\";
//
//    @BeforeAll
//    public static void setup() {
//        formatter = new Mp3FileFormatter();
//    }
//
//    @Test
//    public void testFormatFilename() throws Mp3FileFormatException {
//
//        assertEquals(
//                formatter.format(Path.of(defaultParentPath, "WXXLER_-_Demons_In_My_Blood_(Phonk_remix)_(ru.soundmax.me).mp3"),
//                "WXXLER_-_Demons_In_My_Blood_(Phonk_remix).mp3"
//        );
//
//        assertThrows(Mp3FileFormatException.class, () -> Mp3FileFormatter.removeAd("HXVRMXN_-_スピードデーモン.mp3"));
//    }
}