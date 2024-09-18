package com.github.Leo_Proger.mp3_file_handlers;

import com.github.Leo_Proger.config.Config;
import com.github.Leo_Proger.config.ErrorMessage;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileFormatter {
    // Disable logging of jaudiotagger library
    static {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
    }

    /**
     * Check the correctness of the filename format with regexf
     *
     * @param filename filename to check
     * @return {@code true}, if the filename matches expected format and can be processed further, {@code false} otherwise
     * @see Config#FILENAME_FORMAT
     */
    public static boolean isValidMp3Filename(String filename) {
        return filename.matches(Config.FILENAME_FORMAT);
    }

    /**
     * Start formatting MP3 filename and its metadata
     *
     * @param mp3File MP3 file that needs to be formatted
     * @return new file with formatted filename and metadata
     */
    public Path format(Path mp3File) throws Mp3FileFormattingException, CannotWriteException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, IOException {
        FilenameFormatter filenameFormatter = new FilenameFormatter();
        MetadataFormatter metadataFormatter = new MetadataFormatter();

        String newFilename = filenameFormatter.run(mp3File.getFileName().toString());
        metadataFormatter.run(mp3File, newFilename);

        // Final check for formatting correctness
        if (!isValidMp3Filename(String.valueOf(newFilename))) {
            throw new Mp3FileFormattingException(mp3File, ErrorMessage.INVALID_FORMAT.getMessage());
        }
        return mp3File.getParent().resolve(newFilename);
    }
}
