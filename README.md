# MP3 Editor

## Description

MP3 Editor is a tool for managing MP3 files. It performs the following functions:

- Formats file names
- Extracts and updates file metadata
- Moves files to the specified folder

The program processes MP3 files in a folder, helping to organize digital music collections efficiently.

Key features:

- File name standardization
- Metadata management
- File organization

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

\* What is in brackets is optional \
\* Other formats are not available

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