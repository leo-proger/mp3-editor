package com.github.Leo_Proger.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ImageCleaner {
    private static final Logger LOGGER;

    static {
        LOGGER = LoggerFactory.getLogger(ImageCleaner.class);
    }

    /**
     * Удаляет ненужные изображения в папке (обложки для треков, которые я скачиваю из интернета).
     *
     * @param targetPath папка, в которой нужно удалить изображения
     */
    public static void deleteAllImages(Path targetPath) {
        try (Stream<Path> files = Files.list(targetPath)) {
            files.filter(file -> {
                String fileName = file.getFileName().toString().toLowerCase();
                return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".bmp") || fileName.endsWith(".gif") || fileName.endsWith(".url") || fileName.endsWith(".webp");
            }).forEach(file -> {
                try {
                    Files.delete(file);
                } catch (IOException e) {
                    LOGGER.error("Ошибка при удалении файла \"{}\"", file.getFileName());
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
