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
     * Original filename which remains unchanged during execution of the program.
     * <p>
     * Used to return error messages related to this file.
     *
     * @see FilenameFormatter#formattedFilename
     */
    private String originalFilename;

    /**
     * Filename that is copied from initialFile.
     * Formatting is performed on it.
     *
     * @see FilenameFormatter#originalFilename
     */
    private String formattedFilename;

    /**
     * Main method that runs all other methods
     *
     * @param filename filename to format
     * @return formatted filename
     * @throws Mp3FileFormattingException if {@code filename} isn't a filename, or it doesn't match the template of MP3 file
     * @see Config#FILENAME_FORMAT
     */
    public String run(String filename) throws Mp3FileFormattingException {
        originalFilename = formattedFilename = filename;

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
     * @throws Mp3FileFormattingException if filename doesn't contain "_-_"
     * @see Config#ARTIST_SEPARATORS
     */
    private void replaceArtistSeparatorsWithComma() throws Mp3FileFormattingException {
        // Checking that filename contains artists and song title separated by "_-_"
        if (!formattedFilename.contains("_-_")) {
            throw new Mp3FileFormattingException(Path.of(originalFilename), ErrorMessage.INVALID_FORMAT.getMessage());
        }

        // Divide into parts with artists and song title
        String[] parts = formattedFilename.split("_-_");
        String left = parts[0];
        String right = parts[1];

        // Replace separators with comma
        for (String artistSeparator : ARTIST_SEPARATORS) {
            if (left.contains(artistSeparator)) {
                left = left.replaceAll(artistSeparator, ", ");
            }
        }
        formattedFilename = left + "_-_" + right;
    }


    /**
     * Search for keys in filename (incorrect names of artists) and replace it with values (correct names of artists)
     *
     * @throws Mp3FileFormattingException if filename is incorrect
     * @see Config#CORRECT_ARTIST_NAMES
     */
    private void correctArtistNames() throws Mp3FileFormattingException {
        if (!isValidMp3Filename(formattedFilename)) {
            throw new Mp3FileFormattingException(Path.of(originalFilename), ErrorMessage.INVALID_FORMAT.getMessage());
        }

        // Divide into part with artists and part with song title
        String[] parts = formattedFilename.split("_-_");
        String left = parts[0];
        String right = parts[1];

        // Replace name of artists with correct ones
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
        formattedFilename = String.join(", ", leftWithCorrectedArtistNames) + "_-_" + right;
    }
}
