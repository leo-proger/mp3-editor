package com.github.Leo_Proger.mp3_file_handlers;

import com.github.Leo_Proger.utils.JsonManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArtistManagerTest {
    private ArtistManager artistManager;
    private Path mockJsonFilePath;

//    @Mock
//    private Path mockPath;

    @BeforeEach
    public void setUp() {
        artistManager = new ArtistManager();
        mockJsonFilePath = Path.of("test-artists.json");
    }

    /**
     * Test when an empty set of artists is passed to run method
     * <p>
     * Ensures no action is taken when there are no artists
     */
    @Test
    public void testRunWithEmptySet() {
        Set<String> emptyArtistSet = new LinkedHashSet<>();

        // Redirect system input to simulate user interaction
        System.setIn(new ByteArrayInputStream("\n".getBytes()));

        // Call run method and verify no exceptions are thrown
        assertDoesNotThrow(() -> artistManager.run(emptyArtistSet, mockJsonFilePath));
    }

    /**
     * Test adding artists with user confirmation
     * <p>
     * Verifies that artists are added to the JSON file when user confirms
     */
    @Test
    public void testRunWithUserConfirmation() {
        Set<String> newArtists = new LinkedHashSet<>();
        newArtists.add("Artist1");
        newArtists.add("Artist2");

        // Mock existing artists in JSON file
        Map<String, String> existingArtists = new HashMap<>();
        existingArtists.put("existingartist", "ExistingArtist");

        // Redirect system input to simulate user confirmation
        System.setIn(new ByteArrayInputStream("y\n".getBytes()));

        // Use MockedStatic to mock static methods of JsonManager
        try (MockedStatic<JsonManager> jsonManagerMock = mockStatic(JsonManager.class)) {
            // Stub loading existing data
            jsonManagerMock.when(() -> JsonManager.loadDataFromJson(mockJsonFilePath))
                    .thenReturn(existingArtists);

            artistManager.run(newArtists, mockJsonFilePath);

            // Verify that writeDataToJson was called with updated artists
            jsonManagerMock.verify(() -> JsonManager.writeDataToJson(
                    argThat(map ->
                            map.containsKey("existingartist") &&
                                    map.containsKey("artist1") &&
                                    map.containsKey("artist2")
                    ),
                    eq(mockJsonFilePath)
            ));
        }
    }

    /**
     * Test adding artists with user confirmation and already had artists in JSON file
     * <p>
     * Verifies that artists are added to the JSON file when user confirms but existing artist are remained
     */
    @Test
    public void testRunWithUserConfirmation_HasExistingArtist() {
        Set<String> newArtists = new LinkedHashSet<>();
        newArtists.add("Artist1");
        newArtists.add("Artist2");

        // Mock existing artists in JSON file
        Map<String, String> existingArtists = new HashMap<>();
        existingArtists.put("artist2", "ARTIST2");

        // Redirect system input to simulate user confirmation
        System.setIn(new ByteArrayInputStream("y\n".getBytes()));

        // Use MockedStatic to mock static methods of JsonManager
        try (MockedStatic<JsonManager> jsonManagerMock = mockStatic(JsonManager.class)) {
            // Stub loading existing data
            jsonManagerMock.when(() -> JsonManager.loadDataFromJson(mockJsonFilePath))
                    .thenReturn(existingArtists);

            artistManager.run(newArtists, mockJsonFilePath);

            // Verify that writeDataToJson was called with updated artists but artist2 with ARTIST2 value is remained
            jsonManagerMock.verify(() -> JsonManager.writeDataToJson(
                    argThat(map ->
                            map.get("artist2").equals("ARTIST2") &&
                                    map.containsKey("artist1")
                    ),
                    eq(mockJsonFilePath)
            ));
        }
    }

    /**
     * Test adding artists with user excluding some artists
     * <p>
     * Verifies that specific artists can be excluded from addition
     */
    @Test
    public void testRunWithUserExclusion() {
        Set<String> newArtists = new LinkedHashSet<>();
        newArtists.add("Artist1");
        newArtists.add("Artist2");
        newArtists.add("Artist3");

        // Redirect system input to simulate user excluding artists
        System.setIn(new ByteArrayInputStream("y 0 2\n".getBytes()));

        // Use MockedStatic to mock static methods of JsonManager
        try (MockedStatic<JsonManager> jsonManagerMock = mockStatic(JsonManager.class)) {
            // Stub loading existing data
            jsonManagerMock.when(() -> JsonManager.loadDataFromJson(mockJsonFilePath))
                    .thenReturn(new HashMap<>());

            // Call run method
            artistManager.run(newArtists, mockJsonFilePath);

            // Verify that writeDataToJson was called with only the non-excluded artist
            jsonManagerMock.verify(() -> JsonManager.writeDataToJson(
                    argThat(map ->
                            map.size() == 1 &&
                                    map.containsKey("artist2")
                    ),
                    eq(mockJsonFilePath)
            ));
        }
    }

    /**
     * Test that user rejection prevents artists from being added
     */
    @Test
    public void testRunWithUserRejection() {
        Set<String> newArtists = new LinkedHashSet<>();
        newArtists.add("Artist1");
        newArtists.add("Artist2");

        // Redirect system input to simulate user rejection
        System.setIn(new ByteArrayInputStream("n\n".getBytes()));

        // Use MockedStatic to mock static methods of JsonManager
        try (MockedStatic<JsonManager> jsonManagerMock = mockStatic(JsonManager.class)) {
            // Verify that writeDataToJson is never called
            artistManager.run(newArtists, mockJsonFilePath);
            jsonManagerMock.verifyNoInteractions();
        }
    }

    /**
     * Test handling of duplicate artists with case-insensitive comparison
     */
    @Test
    public void testHandlingOfDuplicateArtistsCaseInsensitive() {
        Set<String> newArtists = new LinkedHashSet<>();
        newArtists.add("Artist1");
        newArtists.add("ARTIST1");
        newArtists.add("artist1");

        // Redirect system input to simulate user rejection
        System.setIn(new ByteArrayInputStream("y\n".getBytes()));

        try (MockedStatic<JsonManager> jsonManagerMock = mockStatic(JsonManager.class)) {
            // Simulate empty existing artists
            jsonManagerMock.when(() -> JsonManager.loadDataFromJson(mockJsonFilePath))
                    .thenReturn(new HashMap<>());

            // Capture the actual map passed to writeDataToJson
            jsonManagerMock.when(() -> JsonManager.writeDataToJson(any(), eq(mockJsonFilePath)))
                    .thenAnswer(invocation -> {
                        Map<String, String> passedMap = invocation.getArgument(0);

                        // Verify map properties
                        assertEquals(1, passedMap.size(), "Should have only one unique artist");
                        assertTrue(passedMap.containsKey("artist1"), "Should have lowercase key");
                        assertEquals("Artist1", passedMap.get("artist1"), "Should preserve the first encountered case");

                        return null;
                    });
            artistManager.run(newArtists, mockJsonFilePath);

            // Verify that writeDataToJson was called
            jsonManagerMock.verify(() -> JsonManager.writeDataToJson(any(), eq(mockJsonFilePath)));
        }
    }

    /**
     * Test error handling when JSON file reading fails
     */
    @Test
    public void testErrorHandlingWhenJsonReadFails() {
        Set<String> newArtists = new LinkedHashSet<>();
        newArtists.add("Artist1");

        // Redirect system input to simulate user confirmation
        System.setIn(new ByteArrayInputStream("y\n".getBytes()));

        // Use MockedStatic to mock static methods of JsonManager
        try (MockedStatic<JsonManager> jsonManagerMock = mockStatic(JsonManager.class)) {
            // Simulate IOException when reading JSON
            jsonManagerMock.when(() -> JsonManager.loadDataFromJson(mockJsonFilePath))
                    .thenThrow(new IOException("Failed to read JSON"));

            // Call run method and verify no unhandled exceptions
            assertDoesNotThrow(() -> artistManager.run(newArtists, mockJsonFilePath));
        }
    }

    /**
     * Test error handling when JSON file writing fails
     */
    @Test
    public void testErrorHandlingWhenJsonWriteFails() {
        Set<String> newArtists = new LinkedHashSet<>();
        newArtists.add("Artist1");

        // Redirect system input to simulate user confirmation
        System.setIn(new ByteArrayInputStream("y\n".getBytes()));

        // Use MockedStatic to mock static methods of JsonManager
        try (MockedStatic<JsonManager> jsonManagerMock = mockStatic(JsonManager.class)) {
            // Stub reading existing data
            jsonManagerMock.when(() -> JsonManager.loadDataFromJson(mockJsonFilePath))
                    .thenReturn(new HashMap<>());

            // Simulate IOException when writing JSON
            jsonManagerMock.when(() -> JsonManager.writeDataToJson(anyMap(), eq(mockJsonFilePath)))
                    .thenThrow(new IOException("Failed to write JSON"));

            // Call run method and verify no unhandled exceptions
            assertDoesNotThrow(() -> artistManager.run(newArtists, mockJsonFilePath));
        }
    }

    /**
     * Test invalid user input for artist exclusion
     */
    @Test
    public void testInvalidUserInputForExclusion() {
        Set<String> newArtists = new LinkedHashSet<>();
        newArtists.add("Artist1");
        newArtists.add("Artist2");

        // Redirect system input to simulate invalid input
        System.setIn(new ByteArrayInputStream("y 10 abc\n".getBytes()));

        // Use MockedStatic to mock static methods of JsonManager
        try (MockedStatic<JsonManager> jsonManagerMock = mockStatic(JsonManager.class)) {
            // Stub loading existing data
            jsonManagerMock.when(() -> JsonManager.loadDataFromJson(mockJsonFilePath))
                    .thenReturn(new HashMap<>());

            // Call run method and verify error is logged but no exception thrown
            assertDoesNotThrow(() -> artistManager.run(newArtists, mockJsonFilePath));
        }
    }
}