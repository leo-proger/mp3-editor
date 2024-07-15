package main;

import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Shell32Util;
import mp3handlers.Mp3Manager;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static final Path SOURCE_PATH = Paths.get(Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_Downloads));
    public static final Path TARGET_PATH = Paths.get("X:\\Music");

    public static void main(String[] args) {
        Mp3Manager.run(true);
        ImageDeleter.deleteAllImages(TARGET_PATH);
    }
}
