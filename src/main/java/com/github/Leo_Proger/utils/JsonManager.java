package com.github.Leo_Proger.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;

import static com.github.Leo_Proger.config.Config.RESOURCES_PATH;


/**
 * Utility class for managing json file operations using Jackson ObjectMapper
 */
public class JsonManager {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Loads and deserializes json data from a resource file.
     *
     * @param <T>           The type of object to deserialize the json into
     * @param fileName      Name of the json file to load
     * @param typeReference TypeReference to handle complex generic types during deserialization
     * @return Deserialized object of type T
     * @throws IOException           If there are issues reading the file or parsing json
     * @throws FileNotFoundException If the specified file does not exist
     */
    public static <T> T loadDataFromResourcesJson(String fileName, TypeReference<T> typeReference) throws IOException {
        File jsonFile = getFileFromResources(fileName);

        try (InputStream stream = new FileInputStream(jsonFile)) {
            return objectMapper.readValue(stream, typeReference);
        } catch (IOException e) {
            throw new IOException("Failed to read data from \"%s\"".formatted(jsonFile.getAbsolutePath()));
        }
    }

    /**
     * Validates and retrieves a json file from the resources path.
     *
     * @param fileName Name of the file to retrieve
     * @return A validated File object
     * @throws FileNotFoundException    If the file does not exist
     * @throws IllegalArgumentException If the path is not a file
     */
    private static File getFileFromResources(String fileName) throws FileNotFoundException {
        File jsonFile = new File(String.valueOf(RESOURCES_PATH), fileName);

        if (!jsonFile.exists()) {
            throw new FileNotFoundException("File \"%s\" not found".formatted(RESOURCES_PATH.resolve(fileName)));
        } else if (!jsonFile.isFile()) {
            throw new IllegalArgumentException("\"%s\" is not a file".formatted(RESOURCES_PATH.resolve(fileName)));
        }
        return jsonFile;
    }

    /**
     * Loads existing data from a json file.
     * <p>
     * Attempts to:
     * <p>
     * 1. Read the json file
     * <p>
     * 2. Convert the json to a map
     *
     * @param jsonFilePath Path to the json file to read
     * @return An Optional containing the map or an empty Optional if reading fails
     */
    public static Map<String, String> loadDataFromJson(Path jsonFilePath) throws IOException {
        JsonNode rootNode = objectMapper.readTree(new File(String.valueOf(jsonFilePath)));
        return objectMapper.convertValue(rootNode, new TypeReference<>() {
        });
    }

    /**
     * Writes a map of data to a json file with pretty-printing.
     *
     * @param data         Map to write to the json file
     * @param jsonFilePath Path to the json file to be updated
     */
    public static void writeDataToJson(Map<String, String> data, Path jsonFilePath) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(String.valueOf(jsonFilePath)), data);
    }
}