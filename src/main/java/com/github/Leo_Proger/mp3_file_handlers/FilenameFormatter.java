package com.github.Leo_Proger.mp3_file_handlers;

import com.github.Leo_Proger.config.Config;
import com.github.Leo_Proger.config.ErrorMessage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.github.Leo_Proger.config.Config.*;
import static com.github.Leo_Proger.mp3_file_handlers.FileFormatter.isValidMp3Filename;

public class FilenameFormatter {
    /**
     * Изначальное имя файла, которое остается неизменным в течение выполнения программы.
     * Используется для возврата сообщений об ошибках, связанных с этим файлом.
     *
     * @see FilenameFormatter#newFilename
     */
    private static String initialFile;

    /**
     * Имя файла, которое копируется из initialFile.
     * Над ним производится форматирование.
     *
     * @see FilenameFormatter#initialFile
     */
    private static String newFilename;

    /**
     * Заменяет символы
     *
     * @see Config#CHARACTERS_TO_REPLACE
     */
    private static void replaceCharacters() {
        StringBuilder result = new StringBuilder();
        for (char c : newFilename.toCharArray()) {
            result.append(CHARACTERS_TO_REPLACE.getOrDefault(c, c));
        }
        newFilename = result.toString();
    }

    /**
     * Удаляет рекламу, находящуюся в BLACKLIST, из имени файла
     *
     * @see Config#BLACKLIST
     */
    private static void removeAds() {
        newFilename = BLACKLIST.stream()
                .reduce(newFilename, (str, ad) -> str.replaceAll("(?i)" + Pattern.quote(ad), ""))
                .trim()
                .replaceAll("(?i)[ _-]+\\.mp3$", ".mp3");
    }

    /**
     * Заменяет все пробелы на нижнее подчеркивание и "поправляет" запятую
     */
    private static void replaceSpacesAndFixCommas() {
        newFilename = newFilename
                .replaceAll(" ", "_")
                .replaceAll("[\\s_]*,[\\s_]*", ", ");
    }

    /**
     * Заменяет все разделители, перечисленные в ARTIST_SEPARATORS, на запятую
     *
     * @throws Mp3FileFormattingException если в имени файла нет "_-_".
     * @see Config#ARTIST_SEPARATORS
     */
    private static void replaceArtistSeparators() throws Mp3FileFormattingException {
        // Проверка того что в имени файла есть исполнители и название трека, разделенные "_-_"
        if (!newFilename.contains("_-_")) {
            throw new Mp3FileFormattingException(Path.of(initialFile), ErrorMessage.FORMAT_INCONSISTENCY_ERROR.getMessage());
        }

        // Разделяет на часть с исполнителями и часть с названием трека
        String[] parts = newFilename.split("_-_");
        String left = parts[0];
        String right = parts[1];

        // Заменяет разделители на запятую
        for (String artistSeparator : ARTIST_SEPARATORS) {
            if (left.contains(artistSeparator)) {
                left = left.replaceAll(artistSeparator, ", ");
            }
        }
        newFilename = left + "_-_" + right;
    }


    /**
     * Ищет в имени файла ключи (некорректные имена исполнителей) и заменяет их на значения (корректные имена исполнителей)
     *
     * @throws Mp3FileFormattingException если имя файла некорректно
     * @see Config#CORRECT_ARTIST_NAMES
     */
    private static void correctArtistNames() throws Mp3FileFormattingException {
        if (!isValidMp3Filename(newFilename)) {
            throw new Mp3FileFormattingException(Path.of(initialFile), ErrorMessage.FORMAT_INCONSISTENCY_ERROR.getMessage());
        }

        // Разделяет на часть с исполнителями и часть с названием трека
        String[] parts = newFilename.split("_-_");
        String left = parts[0];
        String right = parts[1];

        // Заменяет имена исполнителей на корректные
        List<String> leftWithCorrectedArtistNames = new ArrayList<>();
        for (String artist : left.split(", ")) {
            if (CORRECT_ARTIST_NAMES.containsKey(artist.toLowerCase())) {
                leftWithCorrectedArtistNames.add(
                        CORRECT_ARTIST_NAMES.getOrDefault(artist.toLowerCase(), artist.trim())
                );
            } else {
                leftWithCorrectedArtistNames.add(artist);
            }
        }
        newFilename = String.join(", ", leftWithCorrectedArtistNames) + "_-_" + right;
    }

    public static String run(String filename) throws Mp3FileFormattingException {
        initialFile = newFilename = filename;

        replaceCharacters();
        removeAds();
        replaceSpacesAndFixCommas();
        replaceArtistSeparators();
        correctArtistNames();

        return newFilename;
    }
}
