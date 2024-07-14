package mp3handlers;

public class Mp3FormatException extends Exception {
    public Mp3FormatException(String message) {
        super("Файл \"" + message + "\" не удается отформатировать");
    }
}
