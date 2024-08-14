package com.github.Leo_Proger.main;

import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Shell32Util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    public static final Path SOURCE_PATH = Paths.get(Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_Downloads));
    public static final Path TARGET_PATH = Paths.get("X:\\Music");
}
