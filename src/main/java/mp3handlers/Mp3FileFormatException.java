package mp3handlers;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Mp3FileFormatException extends Exception {
    public Mp3FileFormatException(Path fileName) {
        super("Файл \"" + fileName.toAbsolutePath() + "\" не удается отформатировать");
    }
}
