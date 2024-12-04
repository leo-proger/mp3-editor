package com.github.Leo_Proger.mp3_file_handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.Leo_Proger.config.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtistManager.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    /**
     * Orchestrates the main workflow for adding new artists to a JSON file
     *
     * @param artistsSet   A set of new artist names to be potentially added
     * @param jsonFilePath The file path of the JSON file to be updated
     */
    public void run(Set<String> artistsSet, String jsonFilePath) {
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
            LOGGER.info("{}. {}", i, artists.get(i));
        }
        System.out.println();
    }

    /**
     * Prompts the user for input regarding artist addition to the JSON file
     *
     * @param jsonFilePath The path of the JSON file being updated
     * @return A list of user input tokens (first token is confirmation, subsequent tokens are artist indices to exclude)
     */
    private List<String> getUserInput(String jsonFilePath) {
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
    private void processSelectedArtists(List<String> artists, List<String> userInput, String jsonFilePath) {
        try {
            removeSelectedArtists(artists, userInput);
            updateArtistsInJsonFile(artists, jsonFilePath);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            LOGGER.error(ErrorMessage.INCORRECT_INPUT_FORMAT.getMessage());
            LOGGER.error(e.getMessage());
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
     * 1. Load existing artists from the JSON file
     * 2. Convert new artists to a map with lowercase keys
     * 3. Merge new artists with existing artists
     * 4. Write the updated map back to the JSON file
     *
     * @param artists      List of new artists to add
     * @param jsonFilePath Path to the JSON file to be updated
     */
    private void updateArtistsInJsonFile(List<String> artists, String jsonFilePath) {
        loadExistingArtistsFromJson(jsonFilePath)
                .ifPresent(existingArtists -> {
                    Map<String, String> newArtists = convertToLowercaseMap(artists);

                    existingArtists.putAll(newArtists);

                    try {
                        JSON_MAPPER.writerWithDefaultPrettyPrinter()
                                .writeValue(new File(jsonFilePath), existingArtists);
                    } catch (IOException e) {
                        LOGGER.error(ErrorMessage.FAILED_TO_WRITE_DATA.getMessage().formatted(jsonFilePath));
                        LOGGER.error(e.getMessage());
                    }
                });
    }

    /**
     * Converts a list of artists to a map with lowercase keys.
     * <p>
     * Creates a map where:
     * - Keys are lowercase versions of artist names
     * - Values are the original artist names
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

    /**
     * Loads existing artists from a JSON file.
     * <p>
     * Attempts to:
     * 1. Read the JSON file
     * 2. Convert the JSON to a map of artists
     * <p>
     * Handles potential IO exceptions and logs errors.
     *
     * @param jsonFilePath Path to the JSON file to read
     * @return An Optional containing the map of artists, or an empty Optional if reading fails
     */
    private Optional<Map<String, String>> loadExistingArtistsFromJson(String jsonFilePath) {
        try {
            JsonNode rootNode = JSON_MAPPER.readTree(new File(jsonFilePath));
            Map<String, String> artists = JSON_MAPPER.convertValue(rootNode, new TypeReference<>() {
            });
            return Optional.of(artists);
        } catch (IOException e) {
            LOGGER.error(ErrorMessage.FAILED_TO_READ_DATA.getMessage().formatted(jsonFilePath));
            LOGGER.error(e.getMessage());
            return Optional.empty();
        }
    }
}
