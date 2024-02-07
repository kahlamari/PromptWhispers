package in.kahl.promptwhispers.service;

import in.kahl.promptwhispers.model.Game;
import in.kahl.promptwhispers.model.Step;
import in.kahl.promptwhispers.model.StepType;
import in.kahl.promptwhispers.model.dto.PromptCreate;
import in.kahl.promptwhispers.repo.GameRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GameServiceTest {
    private final GameRepo gameRepo = mock(GameRepo.class);
    private final DalleService dalleService = mock(DalleService.class);
    private final CloudinaryService cloudinaryService = mock(CloudinaryService.class);
    private GameService serviceUnderTest;

    @BeforeEach
    void setUp() {
        serviceUnderTest = new GameService(gameRepo, dalleService, cloudinaryService);
    }

    @Test
    void createGameTest_whenGameCreationRequested_thenNewGameReturned() {
        // ARRANGE
        Instant time = Instant.now();
        UUID mockUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class);
             MockedStatic<UUID> mockedUUID = Mockito.mockStatic(UUID.class)) {
            mockedInstant.when(Instant::now).thenReturn(time);
            mockedUUID.when(UUID::randomUUID).thenReturn(mockUUID);

            Game expected = new Game("00000000-0000-0000-0000-000000000000",
                    Collections.emptyList(), time, false);

            when(gameRepo.save(expected)).thenReturn(expected);

            // ACT
            Game actual = serviceUnderTest.createGame();

            // ASSERT
            assertEquals(expected, actual);
            verify(gameRepo).save(expected);
            verifyNoMoreInteractions(gameRepo);
        }
    }

    @Test
    void getGameByIdTest_whenGameExists_thenReturnGame() {
        // ARRANGE
        String id = "1";
        Optional<Game> expectedGame = Optional.of(new Game(id, Collections.emptyList(), Instant.now(), false));
        when(gameRepo.findById(id)).thenReturn(expectedGame);

        // ACT
        Game actualGame = serviceUnderTest.getGameById(id);

        // ASSERT
        assertEquals(expectedGame.get(), actualGame);
        verify(gameRepo).findById(id);
        verifyNoMoreInteractions(gameRepo);
    }

    @Test
    void getGameByIdTest_whenGameNotExists_thenThrowException() {
        // ARRANGE
        Optional<Game> testData = Optional.empty();
        when(gameRepo.findById("N/A")).thenReturn(testData);

        // ACT
        Executable executable = () -> serviceUnderTest.getGameById("1");

        // ASSERT
        assertThrows(NoSuchElementException.class, executable);
    }

    @Test
    void submitPromptTest_when1stPromptSubmitted_thenGameContainsPrompt() {
        // ARRANGE
        Instant time = Instant.now();
        UUID mockUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class);
             MockedStatic<UUID> mockedUUID = Mockito.mockStatic(UUID.class)) {
            mockedInstant.when(Instant::now).thenReturn(time);
            mockedUUID.when(UUID::randomUUID).thenReturn(mockUUID);

            String gameId = "1";
            Optional<Game> gameWithOutPrompt = Optional.of(new Game(gameId, Collections.emptyList(), time, false));
            when(gameRepo.findById(gameId)).thenReturn(gameWithOutPrompt);

            String promptInput = "Sheep jumps over hedge";

            Game expectedGameWithPrompt = new Game(gameId,
                    List.of(new Step(StepType.PROMPT, promptInput)),
                    time, false);
            when(gameRepo.save(expectedGameWithPrompt)).thenReturn(expectedGameWithPrompt);

            PromptCreate userProvidedPrompt = new PromptCreate(promptInput);

            // ACT
            Game actualGameWithPrompt = serviceUnderTest.submitPrompt(gameId, userProvidedPrompt);

            // ASSERT
            assertEquals(expectedGameWithPrompt, actualGameWithPrompt);
            verify(gameRepo).findById(gameId);
            verify(gameRepo).save(expectedGameWithPrompt);
            verifyNoMoreInteractions(gameRepo);
        }
    }

    @Test
    void generateImageTest_whenGameIdProvided_thenGenerateImage() {
        // ARRANGE
        Instant time = Instant.now();
        UUID mockUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class);
             MockedStatic<UUID> mockedUUID = Mockito.mockStatic(UUID.class)) {
            mockedInstant.when(Instant::now).thenReturn(time);
            mockedUUID.when(UUID::randomUUID).thenReturn(mockUUID);

            String gameId = "1";
            String promptInput = "Sheep jumps over hedge";
            Step prompt = new Step(StepType.PROMPT, promptInput);
            Optional<Game> gameWithPrompt = Optional.of(new Game(gameId, List.of(prompt), time, false));
            when(gameRepo.findById(gameId)).thenReturn(gameWithPrompt);

            String imageUrl = "https://example.com/image.png";
            when(dalleService.getGeneratedImageUrl(promptInput)).thenReturn(imageUrl);
            when(cloudinaryService.uploadImage(imageUrl)).thenReturn(imageUrl);

            Step generatedImage = new Step(StepType.IMAGE, imageUrl);
            Game gameWithImageUrl = new Game(gameId,
                    List.of(prompt, generatedImage), time, false);
            when(gameRepo.save(gameWithImageUrl)).thenReturn(gameWithImageUrl);

            // ACT
            Game gameActual = serviceUnderTest.generateImage(gameId);

            // ASSERT
            assertEquals(gameWithImageUrl, gameActual);
            verify(gameRepo).findById(gameId);
            verify(gameRepo).save(gameWithImageUrl);
            verifyNoMoreInteractions(gameRepo);
            verify(dalleService).getGeneratedImageUrl(promptInput);
            verifyNoMoreInteractions(dalleService);
        }
    }
}