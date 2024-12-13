package com.github.Leo_Proger.mp3_file_handlers;

import com.github.Leo_Proger.utils.JsonManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manages a collection of artists, providing functionality to interactively add artists to a JSON file.
 * <p>
 * This class handles the process of displaying new artists, getting user confirmation,
 * and updating a JSON file with artist information while avoiding duplicates.
 */
public class ArtistManager {
    private static final Logger log = LoggerFactory.getLogger(ArtistManager.class);

    /**
     * Orchestrates the main workflow for adding new artists to a JSON file
     *
     * @param artistsSet   A set of new artist names to be potentially added
     * @param jsonFilePath The file path of the JSON file to be updated
     */
    public void run(Set<String> artistsSet, Path jsonFilePath) {
        if (artistsSet.isEmpty()) {
            return;
        }
        List<String> artistList = new ArrayList<>(artistsSet);
        printArtistList(artistList);

        List<String> userInput = getUserInput(jsonFilePath);
        if (isConfirmedByUser(userInput)) {
            processSelectedArtists(artistList, userInput, jsonFilePath);
        }
    }

    /**
     * Prints the list of new artists to the console with numbered indices
     *
     * @param artists The list of new artists to be printed
     */
    private void printArtistList(List<String> artists) {
        System.out.println("\nNew artists:");
        for (int i = 0; i < artists.size(); i++) {
            log.info("{}. {}", i, artists.get(i));
        }
        System.out.println();
    }

    /**
     * Prompts the user for input regarding artist addition to the JSON file
     *
     * @param jsonFilePath The path of the JSON file being updated
     * @return A list of user input tokens (first token is confirmation, subsequent tokens are artist indices to exclude)
     */
    private List<String> getUserInput(Path jsonFilePath) {
        System.out.printf("""
                Add all these to %s?
                You can exclude any artist by specifying their number separated by a space.
                
                Example: "y 1 3 10" or "y" or "n":\s""", jsonFilePath);

        try (Scanner scanner = new Scanner(System.in)) {
            return Arrays.asList(scanner.nextLine().split("\\s+"));
        }
    }

    /**
     * Checks if the user has confirmed adding artists
     *
     * @param input List of user input tokens
     * @return true if the first token is 'y', false otherwise
     */
    private boolean isConfirmedByUser(List<String> input) {
        return input.getFirst().equalsIgnoreCase("y");
    }

    /**
     * Processes the selected artists after user confirmation
     * <p>
     * Handles the workflow of:
     * 1. Removing any artists the user chose to exclude
     * 2. Updating the JSON file with the remaining artists
     * <p>
     * Catches and logs any input parsing errors
     *
     * @param artists      The original list of new artists
     * @param userInput    User's input tokens
     * @param jsonFilePath Path to the JSON file to be updated
     */
    private void processSelectedArtists(List<String> artists, List<String> userInput, Path jsonFilePath) {
        try {
            removeSelectedArtists(artists, userInput);
            updateArtistsInJsonFile(artists, jsonFilePath);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            log.error("Incorrect input", e);
        }
    }

    /**
     * Removes artists from the list based on user-specified indices
     * <p>
     * Converts user input to indices and removes corresponding artists
     * Processes indices in reverse order to prevent index shifting during removal
     *
     * @param artists   The list of artists to potentially remove from
     * @param userInput List of input tokens containing indices to remove
     */
    private void removeSelectedArtists(List<String> artists, List<String> userInput) {
        List<Integer> excludedIndexes = userInput.stream()
                .skip(1)
                .map(Integer::parseInt)
                .sorted(Comparator.reverseOrder())
                .toList();

        for (Integer index : excludedIndexes) {
            artists.remove(index.intValue());
        }
    }

    /**
     * Updates the JSON file with new artists, preventing duplicates
     * <p>
     * Workflow:
     * <p>
     * 1. Load existing artists from the JSON file
     * <p>
     * 2. Convert new artists to a map with lowercase keys
     * <p>
     * 3. Merge new artists with existing artists
     * <p>
     * 4. Write the updated map back to the JSON file
     *
     * @param artists      List of new artists to add
     * @param jsonFilePath Path to the JSON file to be updated
     */
    private void updateArtistsInJsonFile(List<String> artists, Path jsonFilePath) {
        Map<String, String> existingArtists = new HashMap<>();
        try {
            existingArtists = JsonManager.loadDataFromJson(jsonFilePath);
        } catch (IOException e) {
            log.error("Failed to read data from \"{}\"", jsonFilePath, e);
        }

        Map<String, String> newArtists = convertToLowercaseMap(artists);
        existingArtists.putAll(newArtists);

        try {
            JsonManager.writeDataToJson(existingArtists, jsonFilePath);
        } catch (IOException e) {
            log.error("Failed to write data to \"{}\"", jsonFilePath, e);
        }
    }

    /**
     * Converts a list of artists to a map with lowercase keys.
     * <p>
     * Creates a map where:
     * <p>
     * - Keys are lowercase versions of artist names
     * <p>
     * - Values are the original artist names
     * <p>
     * - In case of duplicate lowercase keys, the first value is retained
     *
     * @param artists List of artists to convert
     * @return A map of lowercase artist names to their original names
     */
    private Map<String, String> convertToLowercaseMap(List<String> artists) {
        return artists.stream()
                .collect(Collectors.toMap(
                        String::toLowerCase,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }
}
