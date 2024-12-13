package com.github.Leo_Proger.mp3_file_handlers;

import com.github.Leo_Proger.config.Config;
import com.github.Leo_Proger.exceptions.Mp3FileFormattingException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.github.Leo_Proger.config.Config.*;
import static com.github.Leo_Proger.mp3_file_handlers.FileFormatter.isValidMp3Filename;

public class FilenameFormatter {
    /**
     * Filename that is copied from initialFile.
     * Formatting is performed on it.
     */
    private String formattedFilename;

    private static final Set<String> newArtists = new HashSet<>();

    public static Set<String> getNewArtists() {
        return newArtists;
    }

    /**
     * Main method that runs all other methods
     *
     * @param filename filename to format
     * @return formatted filename
     * @throws Mp3FileFormattingException if {@code filename} isn't a filename, or it doesn't match the template of MP3 file
     * @see Config#FILENAME_FORMAT
     */
    public String run(String filename) throws Mp3FileFormattingException {
        formattedFilename = filename;

        replaceInvalidCharacters();
        removeAds();
        replaceSpacesAndFixCommas();
        replaceArtistSeparatorsWithComma();
        correctArtistNames();

        return formattedFilename;
    }

    /**
     * Replace characters
     *
     * @see Config#CHARACTERS_TO_REPLACE
     */
    private void replaceInvalidCharacters() {
        StringBuilder result = new StringBuilder();
        for (char c : formattedFilename.toCharArray()) {
            result.append(CHARACTERS_TO_REPLACE.getOrDefault(String.valueOf(c), String.valueOf(c)));
        }
        formattedFilename = result.toString();
    }

    /**
     * Remove ads found in BLACKLIST from filename
     *
     * @see Config#BLACKLIST
     */
    private void removeAds() {
        formattedFilename = BLACKLIST.stream()
                .reduce(formattedFilename, (str, ad) -> str.replaceAll("(?i)" + Pattern.quote(ad), ""))
                .trim()
                .replaceAll("(?i)[ _-]+\\.mp3$", ".mp3");
    }

    /**
     * Replace all spaces with underscores and correct commas
     */
    private void replaceSpacesAndFixCommas() {
        formattedFilename = formattedFilename
                .replaceAll(" ", "_")
                .replaceAll("[\\s_]*,[\\s_]*", ", ");
    }

    /**
     * Replace all separators listed in ARTIST_SEPARATORS with commas
     *
     * @throws Mp3FileFormattingException if filename does not contain "_-_"
     * @see Config#ARTIST_SEPARATORS
     */
    private void replaceArtistSeparatorsWithComma() throws Mp3FileFormattingException {
        // Checking that filename contains artists and track title separated by "_-_"
        if (!formattedFilename.contains("_-_")) {
            throw new Mp3FileFormattingException("Invalid filename format");
        }

        // Divide into parts with artists and track title
        String[] parts = formattedFilename.split("_-_");
        String left = parts[0];
        String right = parts[1];

        // Replace separators with comma
        for (String artistSeparator : ARTIST_SEPARATORS) {
            String escapedSeparator = Pattern.quote(artistSeparator);
            left = left.replaceAll("(?i)" + escapedSeparator, ", ");
        }
        formattedFilename = left + "_-_" + right;
    }


    /**
     * Search for keys in filename (incorrect names of artists) and replace it with values (correct names of artists)
     *
     * @throws Mp3FileFormattingException if filename is incorrect
     * @see Config#CORRECT_ARTISTS_NAMES
     */
    private void correctArtistNames() throws Mp3FileFormattingException {
        if (!isValidMp3Filename(formattedFilename)) {
            throw new Mp3FileFormattingException("Invalid filename format");
        }

        // Divide into part with artists and part with track title
        String[] parts = formattedFilename.split("_-_");
        String left = parts[0];
        String right = parts[1];

        // Replace name of artists with correct ones
        List<String> leftWithCorrectedArtistNames = new ArrayList<>();
        for (String artist : left.split(", ")) {
            if (CORRECT_ARTISTS_NAMES.containsKey(artist.toLowerCase())) {
                leftWithCorrectedArtistNames.add(
                        CORRECT_ARTISTS_NAMES.getOrDefault(artist.toLowerCase(), artist.trim())
                );
            } else {
                leftWithCorrectedArtistNames.add(artist);
                newArtists.add(artist);
            }
        }
        formattedFilename = String.join(", ", leftWithCorrectedArtistNames) + "_-_" + right;
    }
}
