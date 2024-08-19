package com.github.Leo_Proger.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
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

    public static final String FILENAME_FORMAT = "^(([а-яА-Яa-zA-Z0-9()\\-_.!$'øØ]+)(_[а-яА-Яa-zA-Z0-9()\\-_.!$'øØ]+)*)(,\\s[а-яА-Яa-zA-Z0-9()\\-_.!$'øØ]+(_[а-яА-Яa-zA-Z0-9()\\-_.!$'øØ]+)*)*_-_([а-яА-Яa-zA-Z0-9()\\-_.,!$'øØ ]+)\\.mp3$";

    /**
     * Символы, которые нужно заменить в строке
     */
    public static Map<String, String> CHARACTERS_TO_REPLACE;

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
    public static Set<String> ARTISTS_EXCLUSIONS;

    /**
     * Разделители между исполнителями, которые нужно заменить на запятую, чтобы было единообразие
     */
    public static List<String> ARTIST_SEPARATORS;

    /**
     * Метод, который присваивает значения из json файлов переменным: CHARACTERS_TO_REPLACE, BLACKLIST,
     * CORRECT_ARTIST_NAMES, ARTISTS_EXCEPTIONS, ARTIST_SEPARATORS.
     */
    private static void loadDataFromJson() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Загрузка данных из characters_to_replace.json
        try (InputStream stream = Config.class.getResourceAsStream("/com/github/Leo_Proger/characters_to_replace.json")) {
            if (stream == null) {
                throw new RuntimeException("characters_to_replace.json не найден");
            }
            CHARACTERS_TO_REPLACE = objectMapper.readValue(stream, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения json файла: " + e.getMessage());
        }

        // Загрузка данных из blacklist.json
        try (InputStream stream = Config.class.getResourceAsStream("/com/github/Leo_Proger/blacklist.json")) {
            if (stream == null) {
                throw new RuntimeException("blacklist.json не найден");
            }
            List<String> blacklistList = objectMapper.readValue(stream, new TypeReference<>() {
            });
            BLACKLIST = new HashSet<>(blacklistList);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения json файла: " + e.getMessage());
        }

        // Загрузка данных из correct_artist_names.json
        try (InputStream stream = Config.class.getResourceAsStream("/com/github/Leo_Proger/correct_artist_names.json")) {
            if (stream == null) {
                throw new RuntimeException("correct_artist_names.json не найден");
            }
            CORRECT_ARTIST_NAMES = objectMapper.readValue(stream, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения json файла: " + e.getMessage());
        }

        // Загрузка данных из artists_exclusions.json
        try (InputStream stream = Config.class.getResourceAsStream("/com/github/Leo_Proger/artists_exclusions.json")) {
            if (stream == null) {
                throw new RuntimeException("artists_exclusions.json не найден");
            }
            List<String> artistsExclusionsList = objectMapper.readValue(stream, new TypeReference<>() {
            });
            ARTISTS_EXCLUSIONS = new HashSet<>(artistsExclusionsList);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения json файла: " + e.getMessage());
        }

        // Загрузка данных из artist_separators.json
        try (InputStream stream = Config.class.getResourceAsStream("/com/github/Leo_Proger/artist_separators.json")) {
            if (stream == null) {
                throw new RuntimeException("artist_separators.json не найден");
            }
            ARTIST_SEPARATORS = objectMapper.readValue(stream, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения json файла: " + e.getMessage());
        }
    }
}
