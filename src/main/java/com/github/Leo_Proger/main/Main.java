package com.github.Leo_Proger.main;

import com.github.Leo_Proger.mp3_file_handlers.FileManager;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        FileManager fileManager = new FileManager();
        fileManager.run(true);

        exitProgram();
    }

    /**
     * Count down 5 second and exit
     */
    private static void exitProgram() {
        for (int i = 5; i >= 0; i--) {
            System.out.print("\rExit in " + i + " sec");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }
        System.exit(0);
    }
}
