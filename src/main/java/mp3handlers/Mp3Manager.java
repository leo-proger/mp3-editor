package mp3handlers;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;

import static main.Main.SOURCE_PATH;
import static main.Main.TARGET_PATH;

public class Mp3Manager {
    private final static Mp3FileFormatter formatter;

    static {
        formatter = new Mp3FileFormatter();
    }

    /**
     * Метод форматирует каждый mp3 файл в SOURCE_PATH
     */
    private static void formatFiles() {
        try (Stream<Path> paths = Files.list(SOURCE_PATH)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(".mp3"))
                    .forEach(path -> {
                        try {
                            formatter.format(path);
                        } catch (Mp3FileFormatException e) {
                            System.err.println("Ошибка форматирования файла \"" + path.getFileName() + "\"");
                        } catch (Exception e) {
                            System.err.println("Ошибка при форматировании файла \"" + path.getFileName() + "\"");
                        }
                    });
        } catch (IOException e) {
            System.err.println("Ошибка при чтении папки");
        }
    }

    /**
     * Метод перемещает все mp3 файлы из SOURCE_PATH в TARGET_PATH
     */
    private static void moveMp3Files() {
        try (Stream<Path> paths = Files.list(SOURCE_PATH)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(".mp3"))
                    .forEach(Mp3Manager::moveAndCheckMp3File);
        } catch (IOException e) {
            System.err.println("Ошибка при чтении папки");
        }
    }


    /**
     * Метод отдельно перемещает файл в папку с музыкой, проверяя на повреждения и существование в целевой папке
     *
     * @param source абсолютный путь к перемещаемому файлу
     */
    private static void moveAndCheckMp3File(Path source) {
        try {
            // Проверка файла на повреждения
            new Mp3File(source);

            // Перемещение файла, если он не существует в целевой папке
            if (Files.exists(TARGET_PATH.resolve(source.getFileName()))) {
                System.err.println("Файл \"" + source.getFileName() + "\" уже существует в целевой папке.");
            } else {
                Files.move(source, TARGET_PATH.resolve(source.getFileName()));
            }
        } catch (InvalidDataException | UnsupportedTagException e) {
            System.err.println("Файл \"" + source.getFileName() + "\" поврежден.");
        } catch (IOException e) {
            System.err.println("Ошибка при перемещении файла \"" + source.getFileName() + "\"");
        }
    }

    public static void run(boolean moveFiles) {
        formatFiles();
        if (moveFiles) {
            moveMp3Files();
        }
    }
}
