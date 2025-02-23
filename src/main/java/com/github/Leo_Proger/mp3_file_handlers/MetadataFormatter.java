package com.github.Leo_Proger.mp3_file_handlers;

import com.github.Leo_Proger.config.Config;
import com.github.Leo_Proger.exceptions.Mp3FileFormattingException;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.images.Artwork;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.github.Leo_Proger.config.Config.ARTISTS_EXCLUSIONS;
import static com.github.Leo_Proger.mp3_file_handlers.FileFormatter.isValidMp3Filename;

public class MetadataFormatter {
    /**
     * Add metadata (track title and artists) to MP3 file.
     * Formatting performs according to the following rules:
     * <p>
     * 1. Underscores are replaced with spaces (excluding artists in ARTISTS_EXCLUSIONS)
     * <p>
     * 2. Comma is replaced with delimiter specified in ARTISTS_DELIMITER_IN_METADATA
     *
     * @param mp3File         Path to MP3 file
     * @param filenameToParse Filename for metadata formatting
     * @throws IOException                In case of input-output errors
     * @throws CannotReadException        If the file cannot be read
     * @throws TagException               In case of tag operations errors
     * @throws InvalidAudioFrameException In case of incorrect audio frame
     * @throws ReadOnlyFileException      If the file is read-only
     * @throws CannotWriteException       If the file cannot be written to
     * @throws Mp3FileFormattingException If the filenameToParse does not match the pattern
     * @see Config#FILENAME_FORMAT
     * @see Config#ARTISTS_DELIMITER_IN_METADATA
     */
    public void run(Path mp3File, String filenameToParse) throws IOException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, CannotWriteException, Mp3FileFormattingException {
        validateFilename(filenameToParse);

        AudioFile audioFile = AudioFileIO.read(mp3File.toFile());
        String[] parts = splitFilename(filenameToParse);
        String formattedArtists = formatArtists(parts[0]);
        String formattedTitle = formatTitle(parts[1]);

        updateTags(audioFile, formattedArtists, formattedTitle);
    }

    /**
     * Check the validity of MP3 file's name
     *
     * @param newFilename New filename
     * @throws Mp3FileFormattingException If the filename doesn't match the pattern
     * @see Config#FILENAME_FORMAT
     */
    private void validateFilename(String newFilename) throws Mp3FileFormattingException {
        if (!isValidMp3Filename(newFilename)) {
            throw new Mp3FileFormattingException("Invalid filename format");
        }
    }

    /**
     * Split MP3 filename into parts
     *
     * @param filename MP3 filename
     * @return Array of 2 elements: artists and track title
     */
    private String[] splitFilename(String filename) {
        return filename.replace(".mp3", "").split("_-_");
    }

    /**
     * Format artists string specifically for metadata
     *
     * @param artists Artists string
     * @return Formatted artists string
     */
    private String formatArtists(String artists) {
        Set<String> artistsForMetadata = new LinkedHashSet<>();
        for (String artist : artists.split(", ")) {
            if (ARTISTS_EXCLUSIONS.contains(artist)) {
                artistsForMetadata.add(artist);
            } else {
                artistsForMetadata.add(artist.replaceAll("_", " "));
            }
        }
        return String.join(Config.ARTISTS_DELIMITER_IN_METADATA.getDelimiter(), artistsForMetadata);
    }

    /**
     * Format track title specifically for metadata
     *
     * @param title Track title
     * @return Formatted track title
     */
    private String formatTitle(String title) {
        return title.replaceAll("_", " ");
    }

    /**
     * Update audio file's tags
     *
     * @param audioFile AudioFile object
     * @param artist    Formatted artist string
     * @param title     Formatted track title
     * @throws TagException In case of tag operations errors
     */
    private void updateTags(AudioFile audioFile, String artist, String title) throws TagException, CannotWriteException, CannotReadException {
        // Preserve artwork and lyrics from original file if available
        Artwork artwork = null;
        if (audioFile.getTag() != null && audioFile.getTag().getFirstArtwork() != null) {
            artwork = audioFile.getTag().getFirstArtwork();
        }
        String lyrics = "";
        if (audioFile.getTag() != null && audioFile.getTag().getFirstField(FieldKey.LYRICS) != null) {
            lyrics = audioFile.getTag().getFirst(FieldKey.LYRICS);
        }
        // Delete other tags
        audioFile.delete();

        // Create new ID3v24 tag and set appropriate fields
        ID3v24Tag newTag = new ID3v24Tag();
        newTag.setField(FieldKey.TITLE, title);
        newTag.setField(FieldKey.ARTIST, artist);
        newTag.setField(FieldKey.LYRICS, lyrics);

        if (artwork != null) {
            newTag.setField(artwork);
        }
        // Set new tag
        audioFile.setTag(newTag);

        // Save changes
        audioFile.commit();
    }
}
