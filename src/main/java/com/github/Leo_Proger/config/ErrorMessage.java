package com.github.Leo_Proger.config;

public enum ErrorMessage {
    FILE_ALREADY_EXISTS_ERROR("Файл уже существует в папке \"%s\""),
    ERROR_MOVING_FILE("Ошибка при перемещении файла"),
    FOLDER_INTERACTION_ERROR("Ошибка взаимодействия с папкой"),
    FILE_FORMATTING_ERROR("Ошибка форматирования файла"),
    FILE_CORRUPTED_ERROR("Файл поврежден"),
    UNKNOWN_ERROR_FORMATTING_FILE("Неизвестная ошибка при форматировании файла"),
    FOLDER_READING_ERROR("Ошибка при чтении папки"),
    FORMAT_INCONSISTENCY_ERROR("Файл не соответствует формату и не может быть отформатирован"),
    ERROR_DISPLAYING_MODIFIER_TRACKS("Ошибка при выводе измененных треков"),
    FILE_IN_USE_BY_ANOTHER_PROCESS_ERROR("Файл занят другим процессом"),
    FILE_ACCESS_RESTRICTED("Доступ к файлу ограничен");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
