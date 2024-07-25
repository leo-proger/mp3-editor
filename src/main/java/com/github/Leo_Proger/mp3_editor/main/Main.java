package com.github.Leo_Proger.mp3_editor.main;

import com.github.Leo_Proger.mp3_editor.mp3handlers.Mp3FileManager;

import java.util.concurrent.TimeUnit;

import static com.github.Leo_Proger.mp3_editor.main.Config.TARGET_PATH;

public class Main {
    public static void main(String[] args) {
        Mp3FileManager.run(true);
        ImageDeleter.deleteAllImages(TARGET_PATH);
        exitProgram();
    }

    /**
     * Отсчитывает 5 секунд и выходит
     */
    private static void exitProgram() {
        System.out.print("Выход через: ");
        for (int i = 5; i >= 0; i--) {
            System.out.print("\rВыход через: " + i + " сек");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }
        System.exit(0);
    }
}
