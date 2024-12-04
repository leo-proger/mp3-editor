# MP3 Editor

## Description

MP3 Editor is a tool for managing MP3 files. It performs the following functions:

- Formats file names
- Extracts and updates file metadata
- Moves files to the specified folder

The program processes MP3 files in a folder, helping to organize digital music collections.

## Examples

- "Artist 1 - Song title.mp3" -> "Artist_1_-_Song_title.mp3"
    - Metadata:
        - Artist: "Artist 1"
        - Title: "Song title"
- "Artist 1, Artist 2 - Another Song Title (some advertising).mp3" -> "Artist_1, Artist_2_-_Another_Song_Title.mp3"
    - Metadata:
        - Artist: "Artist 1; Artist 2"
        - Title: "Another Song Title"
- "Artist-Track" -> **Error!**

Only this format is supported: "Artist 1[, Artist 2, Artist x] - Song name [(advertising)].mp3".
In the end, mp3 editor will detect new artists and offer you to add them to the file.

\* What is in square brackets is optional \
\* Other formats are not available \
\* Advertising to be removed is in `blacklist.json`

## Dependencies

- JAudioTagger - MP3 metadata processing
- Mp3agic - MP3 tag reading/writing (for tests)
- JUnit Jupiter - Unit testing
- Jackson Databind - JSON processing
- Log4j-slf4j2 - Logging
- JetBrains Annotations - For @NotNull annotation

## Requirements

- Java 21 (other version are not tested)
- Maven

## Project Structure

```
MP3 Editor
├───src
│   ├───main
│   │   ├───java
│   │   │   └───com
│   │   │       └───github
│   │   │           └───Leo_Proger
│   │   │               ├───config  # Application configuration classes
│   │   │               ├───main    # Main application entry point
│   │   │               └───mp3_file_handlers   # MP3 file processing utilities
│   │   └───resources
│   │       └───com
│   │           └───github
│   │               └───Leo_Proger  # Project static JSON data
│   └───test
│       ├───java
│       │   └───com
│       │       └───github
│       │           └───Leo_Proger
│       │               └───mp3handlers   # Project unit tests
│       └───resources
│           └───com
│               └───github
│                   └───Leo_Proger
│                       ├───folder_from   # For moving file test
│                       └───folder_to     # For moving file test
```

### About JSON files in resources

`artist_separators.json` - Defines a list of delimiters used to separate multiple artists in filename. These
separators (like '&', 'ft.', 'feat.') will be standardized to ", " for consistent artist representation

`artists_exclusions.json` - Contains a curated list of artists or band names that should be preserved exactly as they
are

`blacklist.json` - Stores patterns of common advertising or irrelevant text frequently appended to filenames. These
snippets will be automatically removed to clean up file names

`characters_to_replace.json` - Maintains a set of special characters or symbols that should be stripped from filenames

`correct_artist_names.json` - A comprehensive mapping of artist name variations. Provides a way to standardize artist
names by correcting common misspellings, alternate spellings, or formatting inconsistencies. Keys are stored in
lowercase to ensure robust matching. The program will detect artists that are not in this file and offer to add them

## Installation & Setup

1. Download the latest release from [Releases](https://github.com/Leo-Proger/mp3-editor/releases)
2. Set the following environment variables on your system
    - `MP3_EDITOR_LOG_FOLDER` - Path for log file (e.g. `C:\mp3_editor\logs`)
    - `MP3_EDITOR_SOURCE_PATH` - Path to input MP3 files (e.g. `C:\Users\<User>\Downloads\`)
    - `MP3_EDITOR_TARGET_PATH` - Path for processed files (e.g. `C:\Music\`)
3. Choose one of these methods:
    - Double-click the executable file (`mp3_editor.exe`)
    - Run via command line: `java -jar mp3_editor.jar`

## Contacts

- Telegram - [Leo_Proger](https://t.me/leo_proger)