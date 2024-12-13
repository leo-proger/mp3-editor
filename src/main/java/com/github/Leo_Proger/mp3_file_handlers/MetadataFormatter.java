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
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.github.Leo_Proger.config.Config.ARTISTS_EXCLUSIONS;
import static com.github.Leo_Proger.mp3_file_handlers.FileFormatter.isValidMp3Filename;

public class MetadataFormatter {
    /**
     * Add metadata (track title and artists) to MP3 file.
     * Formatting is performed according to the following rules:
     * <p>
     * 1. Underscores are replaced with spaces
     * <p>
     * 2. Comma is replaced with semicolon
     *
     * @param mp3File     Path to MP3 file
     * @param newFilename New filename for metadata formatting
     * @throws IOException                In case of input-output errors
     * @throws CannotReadException        If the file cannot be read
     * @throws TagException               In case of tag operations errors
     * @throws InvalidAudioFrameException In case of incorrect audio frame
     * @throws ReadOnlyFileException      If the file is read-only
     * @throws CannotWriteException       If the file cannot be written to
     * @throws Mp3FileFormattingException If the filename doesn't match the pattern
     * @see Config#FILENAME_FORMAT
     */
    public void run(Path mp3File, String newFilename) throws IOException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, CannotWriteException, Mp3FileFormattingException {
        validateFilename(newFilename);

        AudioFile audioFile = AudioFileIO.read(mp3File.toFile());
        String[] parts = splitFilename(newFilename);
        String formattedArtist = formatArtists(parts[0]);
        String formattedTitle = formatTitle(parts[1]);

        updateTags(audioFile, formattedArtist, formattedTitle);

        // Save changes
        audioFile.commit();
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
        return String.join("; ", artistsForMetadata);
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
     * @param audioFile       AudioFile object
     * @param formattedArtist Formatted artist string
     * @param formattedTitle  Formatted track title
     * @throws TagException In case of tag operations errors
     */
    private void updateTags(AudioFile audioFile, String formattedArtist, String formattedTitle) throws TagException {
        Tag originalTag = audioFile.getTagOrCreateAndSetDefault();

        originalTag.setField(FieldKey.ARTIST, formattedArtist);
        originalTag.setField(FieldKey.TITLE, formattedTitle);

        // Create new ID3v1 tag and copy data there
        ID3v1Tag id3v1Tag = new ID3v1Tag();
        id3v1Tag.setArtist(formattedArtist);
        id3v1Tag.setTitle(formattedTitle);

        if (originalTag.hasField(FieldKey.ALBUM)) {
            id3v1Tag.setAlbum(originalTag.getFirst(FieldKey.ALBUM));
        }
        if (originalTag.hasField(FieldKey.YEAR)) {
            id3v1Tag.setYear(originalTag.getFirst(FieldKey.YEAR));
        }

        // Create new ID3v2 tag and copy all metadata
        AbstractID3v2Tag id3v2Tag = new ID3v24Tag();
        for (FieldKey fieldKey : FieldKey.values()) {
            if (originalTag.hasField(fieldKey)) {

                // Process cover art separately
                if (fieldKey.equals(FieldKey.COVER_ART)) {
                    TagField coverArtField = originalTag.getFirstField(FieldKey.COVER_ART);
                    id3v2Tag.setField(coverArtField);
                } else {
                    id3v2Tag.setField(fieldKey, originalTag.getFirst(fieldKey));

                }
            }
        }

        // Update artist and title for ID3v2 tag
        id3v2Tag.setField(FieldKey.ARTIST, formattedArtist);
        id3v2Tag.setField(FieldKey.TITLE, formattedTitle);

        audioFile.setTag(id3v1Tag);
        audioFile.setTag(id3v2Tag);
    }
}
