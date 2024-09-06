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

    public static final String FILENAME_FORMAT = "^(([а-яА-Яa-zA-Z0-9ёЁ()\\-_.!$'øØ]+)(_[а-яА-Яa-zA-Z0-9ёЁ()\\-_.!$'øØ]+)*)(,\\s[а-яА-Яa-zA-Z0-9ёЁ()\\-_.!$'øØ]+(_[а-яА-Яa-zA-Z0-9ёЁ()\\-_.!$'øØ]+)*)*_-_([а-яА-Яa-zA-Z0-9ёЁ()\\-_.,!$'øØ]+)\\.mp3$";

    /**
     * Characters to replace in filename
     */
    public static Map<String, String> CHARACTERS_TO_REPLACE;

    /**
     * Ad to be removed from filename
     */
    public static Set<String> BLACKLIST;

    /**
     * Key is incorrect artist's name; Value is correct artist's name.
     * <p>
     * When searching for artist's name, case is not considered
     */
    public static Map<String, String> CORRECT_ARTIST_NAMES;

    /**
     * Artists who don't need to remove underscore when adding it to metadata
     */
    public static Set<String> ARTISTS_EXCLUSIONS;

    /**
     * Separators between artists that need to be replaced with a comma so that there is uniformity
     */
    public static List<String> ARTIST_SEPARATORS;

    /**
     * Method that assign values from json files to variables above
     */
    private static void loadDataFromJson() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Loading data from characters_to_replace.json
        try (InputStream stream = Config.class.getResourceAsStream("/com/github/Leo_Proger/characters_to_replace.json")) {
            if (stream == null) {
                throw new RuntimeException("characters_to_replace.json not found");
            }
            CHARACTERS_TO_REPLACE = objectMapper.readValue(stream, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Error reading json file: " + e.getMessage());
        }

        // Loading data from blacklist.json
        try (InputStream stream = Config.class.getResourceAsStream("/com/github/Leo_Proger/blacklist.json")) {
            if (stream == null) {
                throw new RuntimeException("blacklist.json not found");
            }
            List<String> blacklistList = objectMapper.readValue(stream, new TypeReference<>() {
            });
            BLACKLIST = new HashSet<>(blacklistList);
        } catch (IOException e) {
            throw new RuntimeException("Error reading json file: " + e.getMessage());
        }

        // Loading data from correct_artist_names.json
        try (InputStream stream = Config.class.getResourceAsStream("/com/github/Leo_Proger/correct_artist_names.json")) {
            if (stream == null) {
                throw new RuntimeException("correct_artist_names.json not found");
            }
            CORRECT_ARTIST_NAMES = objectMapper.readValue(stream, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Error reading json file: " + e.getMessage());
        }

        // Loading data from artists_exclusions.json
        try (InputStream stream = Config.class.getResourceAsStream("/com/github/Leo_Proger/artists_exclusions.json")) {
            if (stream == null) {
                throw new RuntimeException("artists_exclusions.json not found");
            }
            List<String> artistsExclusionsList = objectMapper.readValue(stream, new TypeReference<>() {
            });
            ARTISTS_EXCLUSIONS = new HashSet<>(artistsExclusionsList);
        } catch (IOException e) {
            throw new RuntimeException("Error reading json file: " + e.getMessage());
        }

        // Loading data from artist_separators.json
        try (InputStream stream = Config.class.getResourceAsStream("/com/github/Leo_Proger/artist_separators.json")) {
            if (stream == null) {
                throw new RuntimeException("artist_separators.json not found");
            }
            ARTIST_SEPARATORS = objectMapper.readValue(stream, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Error reading json file: " + e.getMessage());
        }
    }
}
