package com.github.Leo_Proger.mp3_file_handlers;

import com.github.Leo_Proger.config.ErrorMessage;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.github.Leo_Proger.config.Config.ARTISTS_EXCEPTIONS;
import static com.github.Leo_Proger.mp3_file_handlers.FileFormatter.isValidMp3Filename;

public class MetadataFormatter {
    /**
     * Добавляет mp3 файлу метаданные (название трека и исполнителей).
     * Форматирование производится согласно такому формату:
     * <p>
     * 1. Заменяются все нижние подчеркивания на пробелы.
     * <p>
     * 2. Заменяется запятая на точку с запятой.
     */
    public static void run(Path mp3File, String newFilename) throws IOException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, CannotWriteException, Mp3FileFormattingException {
        if (!isValidMp3Filename(newFilename)) {
            throw new Mp3FileFormattingException(mp3File, ErrorMessage.FORMAT_INCONSISTENCY_ERROR.getMessage());
        }
        // Преобразует в аудиофайл. Проверяет на ошибки
        AudioFile audioFile = AudioFileIO.read(mp3File.toFile());

        // Получаем теги из аудиофайла
        ID3v1Tag id3v1Tag = new ID3v1Tag();
        AbstractID3v2Tag id3v2Tag = new ID3v24Tag();

        // Разделяет на часть с исполнителями и часть с названием трека
        String[] parts = newFilename.replace(".mp3", "").split("_-_");

        // Проверяет наличие исполнителей, у которых не надо заменять нижнее подчеркивание на пробел
        Set<String> artistsForMetadata = new LinkedHashSet<>();
        for (String artist : parts[0].split(", ")) {
            if (ARTISTS_EXCEPTIONS.contains(artist)) {
                artistsForMetadata.add(artist);
            } else {
                artistsForMetadata.add(artist.replaceAll("_", " "));
            }
        }
        // Форматированные исполнители и название трека
        String formattedArtistForMetadata = String.join("; ", artistsForMetadata);
        String formattedTitleForMetadata = parts[1].replaceAll("_", " ");

        // Добавляет данные в ID3v1 тег
        id3v1Tag.setArtist(formattedArtistForMetadata);
        id3v1Tag.setTitle(formattedTitleForMetadata);

        audioFile.setTag(id3v1Tag);

        // Добавляет данные в ID3v2 тег
        id3v2Tag.setField(FieldKey.ARTIST, formattedArtistForMetadata);
        id3v2Tag.setField(FieldKey.TITLE, formattedTitleForMetadata);
        id3v2Tag.setField(FieldKey.ARTISTS, formattedArtistForMetadata);

        audioFile.setTag(id3v2Tag);

        // Сохранение изменений
        audioFile.commit();
    }
}
