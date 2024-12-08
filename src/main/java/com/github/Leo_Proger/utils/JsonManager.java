package com.github.Leo_Proger.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.github.Leo_Proger.config.Config.RESOURCES_PATH;


public class JsonManager {
    private static final ObjectMapper objectMapper = new ObjectMapper();
//    private static final Logger LOGGER = LoggerFactory.getLogger(JsonManager.class);

    public static <T> T loadDataFromResourcesJson(String fileName, TypeReference<T> typeReference) {
        File jsonFile = new File(String.valueOf(RESOURCES_PATH), fileName);

        try (InputStream stream = new FileInputStream(jsonFile)) {
            if (!jsonFile.exists() || !jsonFile.isFile()) {
                throw new RuntimeException("File " + fileName + " not found in path: " + RESOURCES_PATH);
            }
            return objectMapper.readValue(stream, typeReference);
        } catch (IOException e) {
            // TODO: Log errors next commit
//            LOGGER.error(ErrorMessage.FAILED_TO_READ_DATA.getMessage().formatted(jsonFile.getAbsolutePath()));
            throw new RuntimeException("Error reading JSON file: " + jsonFile.getAbsolutePath());
        }
    }
}