package com.github.Leo_Proger.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Config {
    static {
        loadDataFromJson();
    }

    public static final Path SOURCE_PATH = Path.of(System.getenv("MP3_EDITOR_SOURCE_PATH"));
    public static final Path TARGET_PATH = Path.of(System.getenv("MP3_EDITOR_TARGET_PATH"));

    public static final String STRICT_FILENAME_FORMAT = "^(([а-яА-Яa-zA-Z0-9()\\-_.!$'øØ]+)(_[а-яА-Яa-zA-Z0-9()\\-_.!$'øØ]+)*)(,\\s[а-яА-Яa-zA-Z0-9()\\-_.!$'øØ]+(_[а-яА-Яa-zA-Z0-9()\\-_.!$'øØ]+)*)*_-_([а-яА-Яa-zA-Z0-9()\\-_.,!$'øØ ]+)\\.mp3$";

    /**
     * Символы, которые нужно заменить в строке
     */
    public static Map<Character, Character> CHARACTERS_TO_REPLACE;

    /**
     * Реклама, которую нужно удалить из имени файла
     */
    public static Set<String> BLACKLIST;

    /**
     * Ключ - некорректное имя исполнителя; значение - корректное имя исполнителя. При поиске имени исполнителя регистр не учитывается
     */
    public static Map<String, String> CORRECT_ARTIST_NAMES;

    /**
     * Исполнители, у которых не нужно убирать нижнее подчеркивание при добавлении в метаданные
     */
    public static Set<String> ARTISTS_EXCEPTIONS;

    /**
     * Разделители между исполнителями, которые нужно заменить на запятую, чтобы было единообразие
     */
    public static List<String> ARTIST_SEPARATORS;

    /**
     * Метод, который присваивает значения из Json файлов переменным: CHARACTERS_TO_REPLACE, BLACKLIST,
     * CORRECT_ARTIST_NAMES, ARTISTS_EXCEPTIONS, ARTIST_SEPARATORS
     */
    private static void loadDataFromJson() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            File charactersToReplaceFile = new File("src/main/resources/characters_to_replace.json");
            CHARACTERS_TO_REPLACE = objectMapper.readValue(charactersToReplaceFile, new TypeReference<>() {
            });

            File blacklistFile = new File("src/main/resources/blacklist.json");
            List<String> blacklistList = objectMapper.readValue(blacklistFile, new TypeReference<>() {
            });
            BLACKLIST = new HashSet<>(blacklistList);

            File correctArtistNamesFile = new File("src/main/resources/correct_artist_names.json");
            CORRECT_ARTIST_NAMES = objectMapper.readValue(correctArtistNamesFile, new TypeReference<>() {
            });

            File artistsExceptionsFile = new File("src/main/resources/artists_exceptions.json");
            List<String> artistsExceptionsList = objectMapper.readValue(artistsExceptionsFile, new TypeReference<>() {
            });
            ARTISTS_EXCEPTIONS = new HashSet<>(artistsExceptionsList);

            File artistSeparatorsFile = new File("src/main/resources/artist_separators.json");
            ARTIST_SEPARATORS = objectMapper.readValue(artistSeparatorsFile, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения Json файла" + e.getMessage());
        }
    }
}
