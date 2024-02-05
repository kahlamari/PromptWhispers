package in.kahl.promptwhispers.service;

import in.kahl.promptwhispers.model.Game;
import in.kahl.promptwhispers.model.Prompt;
import in.kahl.promptwhispers.model.PromptCreate;
import in.kahl.promptwhispers.repo.GameRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GameServiceTest {
    private final GameRepo gameRepo = mock(GameRepo.class);
    private GameService serviceUnderTest;

    @BeforeEach
    void setUp() {
        serviceUnderTest = new GameService(gameRepo);
    }

    @Test
    void createGameTest_whenGameCreationRequested_thenNewGameReturned() {
        // ARRANGE
        Instant time = Instant.now();
        Mockito.mockStatic(Instant.class);
        Mockito.when(Instant.now()).thenReturn(time);

        UUID mockedUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        Mockito.mockStatic(UUID.class);
        when(UUID.randomUUID()).thenReturn(mockedUUID);

        Game expected = new Game("00000000-0000-0000-0000-000000000000",
                Collections.emptyMap(), time, false);

        when(gameRepo.save(expected)).thenReturn(expected);

        // ACT
        Game actual = serviceUnderTest.createGame();

        // ASSERT
        assertEquals(expected, actual);
        verify(gameRepo).save(expected);
        verifyNoMoreInteractions(gameRepo);
    }

    @Test
    void getGameByIdTest_whenGameExists_thenReturnGame() {
        // ARRANGE
        String id = "1";
        Optional<Game> expectedGame = Optional.of(new Game(id, Collections.emptyMap(), Instant.now(), false));
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
        Mockito.mockStatic(Instant.class);
        Mockito.when(Instant.now()).thenReturn(time);

        UUID mockedUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        Mockito.mockStatic(UUID.class);
        when(UUID.randomUUID()).thenReturn(mockedUUID);

        String gameId = "1";
        Optional<Game> gameWithOutPrompt = Optional.of(new Game(gameId, Collections.emptyMap(), Instant.now(), false));
        when(gameRepo.findById(gameId)).thenReturn(gameWithOutPrompt);

        String promptInput = "Sheep jumps over hedge";

        Game expectedGameWithPrompt = new Game(gameId,
                Map.of(0, new Prompt(promptInput)),
                Instant.now(), false);
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