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

public class ArtistManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtistManager.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

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

    private void printArtistList(List<String> artists) {
        System.out.println("\nNew artists:");
        for (int i = 0; i < artists.size(); i++) {
            LOGGER.info("{}. {}", i, artists.get(i));
        }
        System.out.println();
    }

    private List<String> getUserInput(String jsonFilePath) {
        System.out.printf("""
                Add all these to %s?
                You can exclude any artist by specifying their number separated by a space.
                
                Example: "y 1 3 10" or "y" or "n":\s""", jsonFilePath);

        try (Scanner scanner = new Scanner(System.in)) {
            return Arrays.asList(scanner.nextLine().split("\\s+"));
        }
    }

    private boolean isConfirmedByUser(List<String> input) {
        return input.getFirst().equalsIgnoreCase("y");
    }

    private void processSelectedArtists(List<String> artists, List<String> userInput, String jsonFilePath) {
        try {
            removeSelectedArtists(artists, userInput);
            updateArtistsInJsonFile(artists, jsonFilePath);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            LOGGER.error(ErrorMessage.INCORRECT_INPUT_FORMAT.getMessage());
            LOGGER.error(e.getMessage());
        }
    }

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

    private Map<String, String> convertToLowercaseMap(List<String> artists) {
        return artists.stream()
                .collect(Collectors.toMap(
                        String::toLowerCase,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

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
