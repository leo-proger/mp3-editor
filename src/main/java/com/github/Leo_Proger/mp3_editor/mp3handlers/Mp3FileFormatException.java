package com.github.Leo_Proger.mp3_editor.mp3handlers;

import java.nio.file.Path;

public class Mp3FileFormatException extends Exception {
    public Mp3FileFormatException(Path fileName) {
        super("Файл \"" + fileName.toAbsolutePath() + "\" не удается отформатировать");
    }
}
