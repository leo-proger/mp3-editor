package com.github.Leo_Proger.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.Leo_Proger.utils.JsonManager;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Config {
    public static final Path RESOURCES_PATH = Path.of(System.getenv("MP3_EDITOR_RESOURCES_PATH"));
    public static final Path SOURCE_PATH = Path.of(System.getenv("MP3_EDITOR_SOURCE_PATH"));
    public static final Path TARGET_PATH = Path.of(System.getenv("MP3_EDITOR_TARGET_PATH"));

    static {
        loadDataFromJsons();
    }

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
    private static void loadDataFromJsons() {
        CHARACTERS_TO_REPLACE = JsonManager.loadDataFromResourcesJson("characters_to_replace.json", new TypeReference<>() {
        });

        BLACKLIST = JsonManager.loadDataFromResourcesJson("blacklist.json", new TypeReference<>() {
        });

        CORRECT_ARTISTS_NAMES = JsonManager.loadDataFromResourcesJson("correct_artists_names.json", new TypeReference<>() {
        });

        ARTISTS_EXCLUSIONS = JsonManager.loadDataFromResourcesJson("artists_exclusions.json", new TypeReference<>() {
        });

        ARTIST_SEPARATORS = JsonManager.loadDataFromResourcesJson("artist_separators.json", new TypeReference<>() {
        });
    }
}
