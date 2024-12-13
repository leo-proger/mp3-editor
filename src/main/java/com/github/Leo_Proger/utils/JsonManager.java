package com.github.Leo_Proger.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

import static com.github.Leo_Proger.config.Config.RESOURCES_PATH;


public class JsonManager {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T loadDataFromResourcesJson(String fileName, TypeReference<T> typeReference) throws IOException {
        File jsonFile = getFile(fileName);

        try (InputStream stream = new FileInputStream(jsonFile)) {
            return objectMapper.readValue(stream, typeReference);
        } catch (IOException e) {
            throw new IOException("Failed to read data from \"%s\"".formatted(jsonFile.getAbsolutePath()));
        }
    }

    private static File getFile(String fileName) throws FileNotFoundException {
        File jsonFile = new File(String.valueOf(RESOURCES_PATH), fileName);

        if (!jsonFile.exists()) {
            throw new FileNotFoundException("File \"%s\" not found".formatted(RESOURCES_PATH.resolve(fileName)));
        } else if (!jsonFile.isFile()) {
            throw new IllegalArgumentException("\"%s\" is not a file".formatted(RESOURCES_PATH.resolve(fileName)));
        }
        return jsonFile;
    }
}