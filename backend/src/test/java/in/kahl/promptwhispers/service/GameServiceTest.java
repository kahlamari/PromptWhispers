package in.kahl.promptwhispers.service;

import in.kahl.promptwhispers.model.*;
import in.kahl.promptwhispers.model.dto.PromptCreate;
import in.kahl.promptwhispers.model.dto.RoundResponse;
import in.kahl.promptwhispers.repo.GameRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GameServiceTest {
    private final GameRepo gameRepo = mock(GameRepo.class);
    private final UserService userService = mock(UserService.class);
    private final LobbyService lobbyService = mock(LobbyService.class);
    private final DalleService dalleService = mock(DalleService.class);
    private final CloudinaryService cloudinaryService = mock(CloudinaryService.class);

    private final String userEmail = "user@example.com";
    private GameService serviceUnderTest;

    @BeforeEach
    void setUp() {
        serviceUnderTest = new GameService(gameRepo, userService, lobbyService, dalleService, cloudinaryService);
    }

    private Game createEmptyGame() {
        return new Game(new User(userEmail));
    }

    @Test
    void createGameTest_whenGameCreationRequested_thenReturnNewRoundResponse() {
        // ARRANGE
        Instant time = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        UUID mockUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class);
             MockedStatic<UUID> mockedUUID = Mockito.mockStatic(UUID.class)) {
            mockedInstant.when(Instant::now).thenReturn(time);
            mockedUUID.when(UUID::randomUUID).thenReturn(mockUUID);

            OAuth2User mockedPrincipal = mock(OAuth2User.class);
            User testUser = new User(userEmail);
            when(userService.getLoggedInUser(mockedPrincipal)).thenReturn(testUser);
            when(userService.getUserById(testUser.id())).thenReturn(testUser);

            Lobby testLobby = new Lobby(testUser);

            Game expected = new Game(testUser)
                    .withPlayer(testUser)
                    .withGameState(GameState.REQUEST_NEW_PROMPTS);

            when(gameRepo.save(expected)).thenReturn(expected);

            // ACT
            RoundResponse actual = serviceUnderTest.createGame(mockedPrincipal, testLobby);

            // ASSERT
            assertEquals(expected.asRoundResponse(), actual);
            verify(gameRepo).save(expected);
            verifyNoMoreInteractions(gameRepo);
        }
    }

    @Test
    void getGameByIdTest_whenGameExists_thenReturnGame() {
        // ARRANGE
        OAuth2User mockedPrincipal = mock(OAuth2User.class);
        User testUser = new User(userEmail);
        when(userService.getLoggedInUser(mockedPrincipal)).thenReturn(testUser);

        Optional<Game> expectedGame = Optional.of(createEmptyGame());
        when(gameRepo.findById(expectedGame.get().id())).thenReturn(expectedGame);

        // ACT
        RoundResponse actualGame = serviceUnderTest.getGameById(mockedPrincipal, expectedGame.get().id());

        // ASSERT
        assertEquals(expectedGame.get().asRoundResponse(), actualGame);
        verify(gameRepo).findById(expectedGame.get().id());
        verifyNoMoreInteractions(gameRepo);
    }

    @Test
    void getGameByIdTest_whenGameNotExists_thenThrowException() {
        // ARRANGE
        OAuth2User mockedPrincipal = mock(OAuth2User.class);
        User testUser = new User(userEmail);
        when(userService.getLoggedInUser(mockedPrincipal)).thenReturn(testUser);

        Optional<Game> testData = Optional.empty();
        when(gameRepo.findById("N/A")).thenReturn(testData);

        // ACT
        Executable executable = () -> serviceUnderTest.getGameById(mockedPrincipal, "1");

        // ASSERT
        assertThrows(NoSuchElementException.class, executable);
    }

    @Test
    void deleteGameTest_whenGameExists_thenRemoveGame() {
        // ARRANGE
        User user = new User(userEmail);
        Game testGameToDelete = new Game(user);
        Game testGameToKeep = new Game(user);

        User userExpected = user.withGame(testGameToDelete);

        User testUser = userExpected
                .withGame(testGameToKeep);

        OAuth2User mockedPrincipal = mock(OAuth2User.class);
        when(userService.getLoggedInUser(mockedPrincipal)).thenReturn(testUser);

        when(gameRepo.findById(testGameToDelete.id())).thenReturn(Optional.of(testGameToDelete));
        when(userService.removeGame(testUser, testGameToDelete)).thenReturn(userExpected);
        doNothing().when(gameRepo).delete(testGameToDelete);

        // ACT
        serviceUnderTest.deleteGame(mockedPrincipal, testGameToDelete.id());

        // ASSERT
        verify(userService).getLoggedInUser(mockedPrincipal);
        verify(userService).removeGame(testUser, testGameToDelete);
        verifyNoMoreInteractions(userService);

        verify(gameRepo).findById(testGameToDelete.id());
        verify(gameRepo).delete(testGameToDelete);
        verifyNoMoreInteractions(gameRepo);

    }

    @Test
    void deleteGameTest_whenUserDoesNotOwnGame_thenThrowException() {
        // ARRANGE
        User testUser = new User(userEmail);
        Game testGameToDelete = new Game(testUser);
        Game testGameToKeep = new Game(testUser);
        testUser = testUser.withGame(testGameToKeep);

        OAuth2User mockedPrincipal = mock(OAuth2User.class);
        when(userService.getLoggedInUser(mockedPrincipal)).thenReturn(testUser);

        when(gameRepo.findById(testGameToDelete.id())).thenReturn(Optional.of(testGameToDelete));
        // ACT
        Executable executable = () -> serviceUnderTest.deleteGame(mockedPrincipal, testGameToDelete.id());

        // ASSERT
        assertThrows(AccessDeniedException.class, executable);
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

            OAuth2User mockedPrincipal = mock(OAuth2User.class);
            User testUser = new User(userEmail);
            when(userService.getLoggedInUser(mockedPrincipal)).thenReturn(testUser);

            Optional<Game> gameWithOutPrompt = Optional.of(createEmptyGame());
            String gameId = gameWithOutPrompt.get().id();


            String promptInput = "Sheep jumps over hedge";

            Optional<Game> gameWithPrompt = Optional.of(new Game(gameId,
                    gameWithOutPrompt.get().players(),
                    new HashMap<>(Map.of(0, List.of(new Turn(testUser, TurnType.PROMPT, promptInput)))),
                    GameState.REQUEST_NEW_PROMPTS,
                    gameWithOutPrompt.get().createdAt()));
            when(gameRepo.findById(gameId)).thenReturn(gameWithOutPrompt);

            when(gameRepo.save(any(Game.class))).thenReturn(gameWithPrompt.get());

            PromptCreate userProvidedPrompt = new PromptCreate(promptInput);


            // ACT
            RoundResponse actualRoundWithPrompt = serviceUnderTest.submitPrompt(mockedPrincipal, gameId, userProvidedPrompt);

            // ASSERT
            assertEquals(gameWithPrompt.get().asRoundResponse(testUser), actualRoundWithPrompt);
            verify(userService).getLoggedInUser(mockedPrincipal);
            verifyNoMoreInteractions(userService);

            verify(gameRepo).findById(gameId);
            verify(gameRepo).save(gameWithPrompt.get());
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

            OAuth2User mockedPrincipal = mock(OAuth2User.class);
            User testUser = new User(userEmail);
            when(userService.getLoggedInUser(mockedPrincipal)).thenReturn(testUser);

            String gameId = "1";
            String promptInput = "Sheep jumps over hedge";
            User user = new User(userEmail);
            Turn prompt = new Turn(user, TurnType.PROMPT, promptInput);
            Optional<Game> gameWithPrompt = Optional.of(new Game(gameId,
                    List.of(user),
                    new HashMap<>(Map.of(0, List.of(prompt))),
                    GameState.WAIT_FOR_IMAGES,
                    time));
            when(gameRepo.findById(gameId)).thenReturn(gameWithPrompt);

            String imageUrl = "https://example.com/image.png";
            when(dalleService.getGeneratedImageUrl(promptInput)).thenReturn(imageUrl);
            when(cloudinaryService.uploadImage(imageUrl)).thenReturn(imageUrl);

            Turn generatedImage = new Turn(user, TurnType.IMAGE, imageUrl);
            Game gameWithImageUrl = new Game(gameId, List.of(user),
                    Map.of(0, List.of(prompt, generatedImage)), GameState.FINISHED, time);
            when(gameRepo.save(any(Game.class))).thenReturn(gameWithImageUrl);

            // ACT
            RoundResponse roundResponseActual = serviceUnderTest.generateImage(mockedPrincipal, gameId);

            // ASSERT
            assertEquals(gameWithImageUrl.asRoundResponse(), roundResponseActual);
            verify(gameRepo, times(2)).findById(gameId);
            verify(gameRepo).save(gameWithImageUrl);
            verifyNoMoreInteractions(gameRepo);
            verify(dalleService).getGeneratedImageUrl(promptInput);
            verifyNoMoreInteractions(dalleService);
            verify(cloudinaryService).uploadImage(imageUrl);
            verifyNoMoreInteractions(cloudinaryService);
        }
    }

    @Test
    void submitPrompt_whenGameFinished_thenThrowException() {
        // ARRANGE
        String gameId = "1";
        Optional<Game> gameWith3Images = Optional.of(new Game(gameId,
                null,
                Collections.emptyMap(),
                GameState.FINISHED,
                Instant.now()));
        when(gameRepo.findById(gameId)).thenReturn(gameWith3Images);

        OAuth2User mockedPrincipal = mock(OAuth2User.class);
        User testUser = new User(userEmail);
        when(userService.getLoggedInUser(mockedPrincipal)).thenReturn(testUser);

        String promptInput = "Sheep jumps over hedge";
        PromptCreate userProvidedPrompt = new PromptCreate(promptInput);

        // ACT
        Executable executable = () -> serviceUnderTest.submitPrompt(mockedPrincipal, gameId, userProvidedPrompt);

        // ASSERT
        assertThrows(IllegalArgumentException.class, executable);
    }
}