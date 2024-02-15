package in.kahl.promptwhispers.service;

import in.kahl.promptwhispers.model.Lobby;
import in.kahl.promptwhispers.model.User;
import in.kahl.promptwhispers.repo.LobbyRepo;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LobbyServiceTest {
    private final LobbyRepo lobbyRepo = mock(LobbyRepo.class);
    private final UserService userService = mock(UserService.class);
    private final String userEmail = "user@example.com";
    private LobbyService serviceUnderTest;

    @BeforeEach
    void setUp() {
        serviceUnderTest = new LobbyService(lobbyRepo, userService);
    }

    @Test
    void createLobbyTest_whenLobbyCreationRequest_thenNewLobbyReturned() {
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

            Lobby expected = new Lobby(testUser);

            when(lobbyRepo.save(expected)).thenReturn(expected);

            // ACT
            Lobby actual = serviceUnderTest.createLobby(mockedPrincipal);

            // ASSERT
            assertEquals(expected, actual);
            verify(lobbyRepo).save(expected);
            verifyNoMoreInteractions(lobbyRepo);
        }
    }

    @Test
    void getLobbyByIdTest_whenLobbyExists_thenReturnLobby() {
        // ARRANGE
        String id = "1";
        User testUser = new User(userEmail);
        Optional<Lobby> expectedLobby = Optional.of(new Lobby(id, testUser, List.of(testUser), false, false, Instant.now()));
        when(lobbyRepo.findById(id)).thenReturn(expectedLobby);

        // ACT
        Lobby actualLobby = serviceUnderTest.getLobbyById(id);

        // ASSERT
        assertEquals(expectedLobby.get(), actualLobby);
        verify(lobbyRepo).findById(id);
        verifyNoMoreInteractions(lobbyRepo);
    }

    @Test
    void getLobbyByIdTest_whenLobbyNotExists_thenThrowException() {
        // ARRANGE
        Optional<Lobby> testData = Optional.empty();
        when(lobbyRepo.findById("N/A")).thenReturn(testData);

        // ACT
        Executable executable = () -> serviceUnderTest.getLobbyById("1");

        // ASSERT
        assertThrows(NoSuchElementException.class, executable);
    }

    @Test
    void joinLobbyTest_whenRequestToJoin_thenReturnLobbyWithNewPlayer() {
        // ARRANGE
        User playerHost = new User(userEmail);
        User playerAlreadyJoined = new User("2" + userEmail);
        User playerToJoin = new User("3" + userEmail);

        OAuth2User mockedPrincipal = mock(OAuth2User.class);
        when(userService.getLoggedInUser(mockedPrincipal)).thenReturn(playerToJoin);

        Optional<Lobby> lobbyInput = Optional.of(new Lobby("1", playerHost, new ArrayList<>(List.of(playerHost, playerAlreadyJoined)), false, false, Instant.now()));
        when(lobbyRepo.findById(lobbyInput.get().id())).thenReturn(lobbyInput);

        Lobby lobbyExpected = new Lobby(lobbyInput.get().id(), playerHost, List.of(playerHost, playerAlreadyJoined, playerToJoin), false, false, lobbyInput.get().createdAt());
        when(lobbyRepo.save(lobbyExpected)).thenReturn(lobbyExpected);

        // ACT
        Lobby lobbyActual = serviceUnderTest.joinLobby(mockedPrincipal, lobbyInput.get().id());

        // ASSERT
        assertTrue(lobbyActual.players().contains(playerToJoin));
        verify(lobbyRepo).findById(lobbyInput.get().id());
        verify(lobbyRepo).save(lobbyExpected);
        verifyNoMoreInteractions(lobbyRepo);
    }

    @Test
    void leaveLobbyTest_whenRequestToLeave_thenReturnLobbyWithoutPlayer() {
        // ARRANGE
        User playerHost = new User(userEmail);
        User playerAlreadyJoined = new User("2" + userEmail);
        User playerToLeave = new User("3" + userEmail);

        OAuth2User mockedPrincipal = mock(OAuth2User.class);
        when(userService.getLoggedInUser(mockedPrincipal)).thenReturn(playerToLeave);

        Optional<Lobby> lobbyInput = Optional.of(new Lobby("1", playerHost, new ArrayList<>(List.of(playerHost, playerAlreadyJoined, playerToLeave)), false, false, Instant.now()));
        when(lobbyRepo.findById(lobbyInput.get().id())).thenReturn(lobbyInput);

        Lobby lobbyExpected = new Lobby(lobbyInput.get().id(), playerHost, List.of(playerHost, playerAlreadyJoined), false, false, lobbyInput.get().createdAt());
        when(lobbyRepo.save(lobbyExpected)).thenReturn(lobbyExpected);

        // ACT
        Lobby lobbyActual = serviceUnderTest.leaveLobby(mockedPrincipal, lobbyInput.get().id());

        // ASSERT
        assertFalse(lobbyActual.players().contains(playerToLeave));
        verify(lobbyRepo).findById(lobbyInput.get().id());
        verify(lobbyRepo).save(lobbyExpected);
        verifyNoMoreInteractions(lobbyRepo);
    }

    @Test
    void deleteLobbyTest_whenUserIsHost_thenRemoveLobby() {
        // ARRANGE
        String id = "1";
        User host = new User(userEmail);
        OAuth2User mockedPrincipal = mock(OAuth2User.class);
        when(userService.getLoggedInUser(mockedPrincipal)).thenReturn(host);

        Optional<Lobby> lobbyToDelete = Optional.of(new Lobby(id, host, List.of(host), false, false, Instant.now()));
        when(lobbyRepo.findById(id)).thenReturn(lobbyToDelete);
        doNothing().when(lobbyRepo).delete(lobbyToDelete.get());

        // ACT
        serviceUnderTest.deleteLobby(mockedPrincipal, id);

        // ASSERT
        verify(userService).getLoggedInUser(mockedPrincipal);
        verifyNoMoreInteractions(userService);

        verify(lobbyRepo).findById(id);
        verify(lobbyRepo).delete(lobbyToDelete.get());
        verifyNoMoreInteractions(lobbyRepo);
    }

    @Test
    void deleteLobbyTest_whenUserIsNotHost_thenThrowException() {
        // ARRANGE
        String id = "1";
        User host = new User(userEmail);
        User notHost = new User("not_host" + userEmail);
        OAuth2User mockedPrincipal = mock(OAuth2User.class);
        when(userService.getLoggedInUser(mockedPrincipal)).thenReturn(notHost);

        Optional<Lobby> lobbyToDelete = Optional.of(new Lobby(id, host, List.of(host), false, false, Instant.now()));
        when(lobbyRepo.findById(id)).thenReturn(lobbyToDelete);

        // ACT
        Executable executable = () -> serviceUnderTest.deleteLobby(mockedPrincipal, id);

        // ASSERT
        assertThrows(AccessDeniedException.class, executable);
    }
}