package com.github.Leo_Proger.config;

public enum ArtistsDelimiterForMetadata {
    SEMICOLON("; "),
    COMMA(", "),
    SLASH(" / "),
    AMPERSAND(" & ");

    private final String delimiter;

    ArtistsDelimiterForMetadata(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getDelimiter() {
        return delimiter;
    }
}
