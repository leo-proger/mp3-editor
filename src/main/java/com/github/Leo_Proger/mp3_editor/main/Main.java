package com.github.Leo_Proger.mp3_editor.main;

import com.github.Leo_Proger.mp3_editor.mp3handlers.Mp3Manager;
import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Shell32Util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class Main {
    public static final Path SOURCE_PATH = Paths.get(Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_Downloads));
    public static final Path TARGET_PATH = Paths.get("X:\\Music");

    public static void main(String[] args) {
        Mp3Manager.run(true);
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
