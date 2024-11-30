package com.github.Leo_Proger.config;

public enum ErrorMessage {
    FILE_ALREADY_EXISTS("File already exists in folder \"%s\""),
    FILE_CORRUPTED("File corrupted"),
    UNABLE_TO_MOVE_FILE("Unable to move file"),
    UNABLE_TO_READ_FOLDER("Unable to read folder"),
    INVALID_FILENAME_FORMAT("Invalid filename format"),
    FILE_IN_USE_BY_ANOTHER_PROCESS("File in use by another process"),
    FILE_ACCESS_DENIED("File access denied"),
    UNKNOWN("Unknown error formatting file");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
