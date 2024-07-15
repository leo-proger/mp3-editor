package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ImageDeleter {

    /**
     * Метод, удаляющий ненужные изображения в папке (обложки для треков, которые я скачиваю из интернета)
     *
     * @param targetPath папка, в которой нужно удалить изображения
     */
    public static void deleteAllImages(Path targetPath) {
        try (Stream<Path> files = Files.list(targetPath)) {
            files.filter(file -> {
                String fileName = file.getFileName().toString().toLowerCase();
                return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".bmp") || fileName.endsWith(".gif") || fileName.endsWith(".url");
            }).forEach(file -> {
                try {
                    Files.delete(file);
                } catch (IOException e) {
                    System.err.println("Ошибка при удалении файла \"" + file.getFileName() + "\"");
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
