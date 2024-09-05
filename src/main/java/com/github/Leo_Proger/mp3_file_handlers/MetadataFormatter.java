package com.github.Leo_Proger.mp3_file_handlers;

import com.github.Leo_Proger.config.Config;
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

import static com.github.Leo_Proger.config.Config.ARTISTS_EXCLUSIONS;
import static com.github.Leo_Proger.mp3_file_handlers.FileFormatter.isValidMp3Filename;

public class MetadataFormatter {
    /**
     * Добавляет MP3 файлу метаданные (название трека и исполнителей).
     * Форматирование производится согласно следующим правилам:
     * <p>
     * 1. Нижние подчеркивания заменяются на пробелы
     * <p>
     * 2. Запятая заменяется на точку с запятой
     *
     * @param mp3File     Путь к MP3 файлу
     * @param newFilename Новое имя файла для форматирования метаданных
     * @throws IOException                При ошибках ввода-вывода
     * @throws CannotReadException        При невозможности прочитать файл
     * @throws TagException               При ошибках работы с тегами
     * @throws InvalidAudioFrameException При некорректном аудио фрейме
     * @throws ReadOnlyFileException      Если файл доступен только для чтения
     * @throws CannotWriteException       При невозможности записи в файл
     * @throws Mp3FileFormattingException Если имя файла не соответствует шаблону
     * @see Config#FILENAME_FORMAT
     */
    public void run(Path mp3File, String newFilename) throws IOException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, CannotWriteException, Mp3FileFormattingException {
        validateFilename(mp3File, newFilename);

        AudioFile audioFile = readAudioFile(mp3File);
        String[] parts = splitFilename(newFilename);
        String formattedArtist = formatArtists(parts[0]);
        String formattedTitle = formatTitle(parts[1]);

        updateTags(audioFile, formattedArtist, formattedTitle);
        saveChanges(audioFile);
    }

    /**
     * Проверяет валидность имени MP3 файла.
     *
     * @param mp3File     Путь к MP3 файлу
     * @param newFilename Новое имя файла
     * @throws Mp3FileFormattingException Если имя файла не соответствует шаблону
     * @see Config#FILENAME_FORMAT
     */
    private void validateFilename(Path mp3File, String newFilename) throws Mp3FileFormattingException {
        if (!isValidMp3Filename(newFilename)) {
            throw new Mp3FileFormattingException(mp3File, ErrorMessage.INVALID_FORMAT.getMessage());
        }
    }

    /**
     * Читает аудиофайл.
     *
     * @param mp3File Путь к MP3 файлу
     * @return AudioFile объект
     * @throws CannotReadException        При невозможности прочитать файл
     * @throws IOException                При ошибках ввода-вывода
     * @throws TagException               При ошибках работы с тегами
     * @throws ReadOnlyFileException      Если файл доступен только для чтения
     * @throws InvalidAudioFrameException При некорректном аудио фрейме
     */
    private AudioFile readAudioFile(Path mp3File) throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
        return AudioFileIO.read(mp3File.toFile());
    }

    /**
     * Разделяет имя MP3 файла на части.
     *
     * @param filename Имя MP3 файла
     * @return Массив из двух элементов: исполнители и название трека
     */
    private String[] splitFilename(String filename) {
        return filename.replace(".mp3", "").split("_-_");
    }

    /**
     * Форматирует строку с исполнителями специально для метаданных.
     *
     * @param artists Строка с исполнителями
     * @return Отформатированная строка с исполнителями
     */
    private String formatArtists(String artists) {
        Set<String> artistsForMetadata = new LinkedHashSet<>();
        for (String artist : artists.split(", ")) {
            if (ARTISTS_EXCLUSIONS.contains(artist)) {
                artistsForMetadata.add(artist);
            } else {
                artistsForMetadata.add(artist.replaceAll("_", " "));
            }
        }
        return String.join("; ", artistsForMetadata);
    }

    /**
     * Форматирует название трека специально для метаданных.
     *
     * @param title Название трека
     * @return Отформатированное название трека
     */
    private String formatTitle(String title) {
        return title.replaceAll("_", " ");
    }

    /**
     * Обновляет теги аудиофайла.
     *
     * @param audioFile       AudioFile объект
     * @param formattedArtist Отформатированная строка с исполнителями
     * @param formattedTitle  Отформатированное название трека
     * @throws TagException При ошибках работы с тегами
     */
    private void updateTags(AudioFile audioFile, String formattedArtist, String formattedTitle) throws TagException {
        ID3v1Tag id3v1Tag = new ID3v1Tag();
        AbstractID3v2Tag id3v2Tag = new ID3v24Tag();

        id3v1Tag.setArtist(formattedArtist);
        id3v1Tag.setTitle(formattedTitle);
        audioFile.setTag(id3v1Tag);

        id3v2Tag.setField(FieldKey.ARTIST, formattedArtist);
        id3v2Tag.setField(FieldKey.TITLE, formattedTitle);
        id3v2Tag.setField(FieldKey.ARTISTS, formattedArtist);
        audioFile.setTag(id3v2Tag);
    }

    /**
     * Сохраняет изменения в аудиофайле.
     *
     * @param audioFile AudioFile объект
     * @throws CannotWriteException При невозможности записи в файл
     */
    private void saveChanges(AudioFile audioFile) throws CannotWriteException {
        audioFile.commit();
    }
}
