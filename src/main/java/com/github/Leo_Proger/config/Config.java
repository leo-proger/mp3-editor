package com.github.Leo_Proger.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.Leo_Proger.utils.JsonManager;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Config {
    static {
        loadDataFromJson();
    }

    public static final Path SOURCE_PATH = Path.of(System.getenv("MP3_EDITOR_SOURCE_PATH"));
    public static final Path TARGET_PATH = Path.of(System.getenv("MP3_EDITOR_TARGET_PATH"));

    public static final String FILENAME_FORMAT = "^(([а-яА-Яa-zA-Z0-9ёЁ()\\-_.!$']+)(_[а-яА-Яa-zA-Z0-9ёЁ()\\-_.!$']+)*)(,\\s[а-яА-Яa-zA-Z0-9ёЁ()\\-_.!$']+(_[а-яА-Яa-zA-Z0-9ёЁ()\\-_.!$']+)*)*_-_([а-яА-Яa-zA-Z0-9ёЁ()\\-_.,!$'`&]+)\\.mp3$";

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
    public static Map<String, String> CORRECT_ARTISTS_NAMES;

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
        // Loading data from characters_to_replace.json
        CHARACTERS_TO_REPLACE = JsonManager.loadJsonFromResource("characters_to_replace.json", new TypeReference<>() {
        });

        // Loading data from blacklist.json
        BLACKLIST = JsonManager.loadJsonFromResource("blacklist.json", new TypeReference<>() {
        });

        // Loading data from correct_artists_names.json
        CORRECT_ARTISTS_NAMES = JsonManager.loadJsonFromResource("correct_artists_names.json", new TypeReference<>() {
        });

        // Loading data from artists_exclusions.json
        ARTISTS_EXCLUSIONS = JsonManager.loadJsonFromResource("artists_exclusions.json", new TypeReference<>() {
        });

        // Loading data from artist_separators.json
        ARTIST_SEPARATORS = JsonManager.loadJsonFromResource("artist_separators.json", new TypeReference<>() {
        });
    }
}
