package com.github.Leo_Proger.exceptions;

import java.nio.file.Path;

public class Mp3FileFormattingException extends Exception {
    private final Path filename;

    public Mp3FileFormattingException(Path fileName, String message) {
        super(message);
        this.filename = fileName;
    }

    public Path getFilename() {
        return filename;
    }
}
