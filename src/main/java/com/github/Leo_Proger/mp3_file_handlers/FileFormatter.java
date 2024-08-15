package com.github.Leo_Proger.mp3_file_handlers;

import com.github.Leo_Proger.config.Config;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileFormatter {
    // Отключает логирование библиотеки jaudiotagger
    static {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
    }

    /**
     * Проверяет регулярным выражением корректность формата имени файла
     *
     * @param filename проверяемое имя файла
     * @return {@code true}, если имя файла соответствует ожидаемому формату и может быть
     * обработано дальше, {@code false} в противном случае
     * @throws IllegalArgumentException если переданное имя файла является пустой строкой
     * @see Config#STRICT_FILENAME_FORMAT
     */
    public static boolean isValidMp3Filename(String filename) {
        return filename.matches(Config.STRICT_FILENAME_FORMAT);
    }

    /**
     * Запускает удаление рекламы, форматирование имени mp3 файла, форматирование метаданных mp3 файла и сохраняет изменения
     *
     * @param mp3File файл расширения mp3, который нужно отформатировать
     * @return новый файл с отформатированными именем и метаданными
     */
    public Path format(Path mp3File) throws Mp3FileFormattingException, CannotWriteException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, IOException {
        String newFilename = FilenameFormatter.run(mp3File.getFileName().toString());
        MetadataFormatter.run(mp3File, newFilename);

        return mp3File.getParent().resolve(newFilename);
    }
}
