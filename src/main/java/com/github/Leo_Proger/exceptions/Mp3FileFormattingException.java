package com.github.Leo_Proger.exceptions;

import java.nio.file.Path;

public class Mp3FileFormattingException extends Exception {
    public final Path FILENAME;
    public final String MESSAGE;

    public Mp3FileFormattingException(Path fileName, String message) {
        super();

        this.FILENAME = fileName;
        this.MESSAGE = message;
    }
}
