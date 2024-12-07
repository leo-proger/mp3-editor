package com.github.Leo_Proger.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.Leo_Proger.config.Config;

import java.io.IOException;
import java.io.InputStream;


public class JsonManager {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T loadJsonFromResource(String fileName, TypeReference<T> typeReference) {
        try (InputStream stream = Config.class.getResourceAsStream("/com/github/Leo_Proger/" + fileName)) {
            if (stream == null) {
                throw new RuntimeException(fileName + " not found");
            }
            return objectMapper.readValue(stream, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Error reading json file: " + e.getMessage());
        }
    }
}