package in.kahl.promptwhispers.service;

import in.kahl.promptwhispers.exception.GoogleEmailNotFoundException;
import in.kahl.promptwhispers.model.Game;
import in.kahl.promptwhispers.model.User;
import in.kahl.promptwhispers.model.dto.UserResponse;
import in.kahl.promptwhispers.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private final UserRepo userRepo = mock(UserRepo.class);
    private UserService serviceUnderTest;

    private final String userEmail = "user@example.com";

    @BeforeEach
    void setUp() {
        serviceUnderTest = new UserService(userRepo);
    }

    @Test
    void getLoggedInUserTest_whenProvideOAuthUser_thenReturnUserResponse() {
        // ARRANGE
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn(userEmail);

        User user = new User(userEmail);
        when(userRepo.getUserByEmail(userEmail)).thenReturn(user);
        UserResponse userResponseExpected = new UserResponse(user);

        // ACT
        UserResponse userResponseActual = serviceUnderTest.getLoggedInUserAsUserResponse(oAuth2User);

        // ASSERT
        assertEquals(userResponseExpected, userResponseActual);
        verify(userRepo).getUserByEmail(userEmail);
        verifyNoMoreInteractions(userRepo);
    }

    @Test
    void getLoggedInUserTest_whenProvideNull_thenReturnNull() {
        // ARRANGE & ACT
        UserResponse userResponseActual = serviceUnderTest.getLoggedInUserAsUserResponse(null);

        // ASSERT
        assertNull(userResponseActual);
    }

    @Test
    void getLoggedInUserTest_whenEmptyEmail_thenReturnNull() {
        // ARRANGE
        OAuth2User oAuth2User = mock(OAuth2User.class);
        String emptyUserEmail = "";
        when(oAuth2User.getAttribute("email")).thenReturn(emptyUserEmail);

        // ACT
        Executable executable = () -> serviceUnderTest.getLoggedInUserAsUserResponse(oAuth2User);

        // ASSERT
        assertThrows(GoogleEmailNotFoundException.class, executable);
    }

    @Test
    void getLoggedInUserTest_whenEmailNull_thenThrowException() {
        // ARRANGE
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn(null);

        // ACT
        Executable executable = () -> serviceUnderTest.getLoggedInUserAsUserResponse(oAuth2User);

        // ASSERT
        assertThrows(GoogleEmailNotFoundException.class, executable);
    }

    @Test
    void getLoggedInUserTest_whenUserNotInDB_thenReturnNull() {
        // ARRANGE
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn(userEmail);
        when(userRepo.getUserByEmail(userEmail)).thenReturn(null);

        // ACT
        UserResponse userResponseActual = serviceUnderTest.getLoggedInUserAsUserResponse(oAuth2User);

        // ASSERT
        assertNull(userResponseActual);
    }

    @Test
    void saveNewUserTest_whenUserNotExists_thenSaveNewUser() {
        // ARRANGE
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn(userEmail);
        when(userRepo.existsByEmail(userEmail)).thenReturn(false);

        User testUser = new User(userEmail);
        when(userRepo.save(testUser)).thenReturn(testUser);

        // ACT
        boolean actual = serviceUnderTest.saveNewUser(oauth2User);

        // ASSERT
        assertTrue(actual);
        verify(userRepo).existsByEmail(userEmail);
        verify(userRepo).save(any(User.class));
        verifyNoMoreInteractions(userRepo);
    }

    @Test
    void saveNewUserTest_whenUserExists_thenReturnTrue() {
        // ARRANGE
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn(userEmail);
        when(userRepo.existsByEmail(userEmail)).thenReturn(true);

        // ACT
        boolean actual = serviceUnderTest.saveNewUser(oauth2User);

        // ASSERT
        assertTrue(actual);
        verify(userRepo).existsByEmail(userEmail);
        verifyNoMoreInteractions(userRepo);
    }

    @Test
    void saveNewUserTest_whenOAuthUserHasNoEmail_thenReturnFalse() {
        // ARRANGE
        String emptyUserEmail = "";
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn(emptyUserEmail);

        // ACT
        boolean actual = serviceUnderTest.saveNewUser(oauth2User);

        // ASSERT
        assertFalse(actual);
    }

    @Test
    void getAllGamesTest_whenGamesExists_thenReturnGames() {
        // ARRANGE
        Game testGame1 = new Game();
        Game testGame2 = new Game();
        User testUser = new User(userEmail)
                .withGame(testGame1)
                .withGame(testGame2);

        when(userRepo.findById(testUser.id())).thenReturn(Optional.of(testUser));

        // ACT
        List<String> gameListActual = serviceUnderTest.getAllGameIds(testUser.id());

        // ASSERT
        assertEquals(testUser.gameIds(), gameListActual);
    }

    @Test
    void getAllGamesTest_whenUserNotExists_thenThrowException() {
        // ARRANGE

        // ACT
        Executable executable = () -> serviceUnderTest.getAllGameIds("not_existent_id");

        // ASSERT
        assertThrows(NoSuchElementException.class, executable);
    }

    @Test
    void removeGameTest_whenGameExists_thenRemoveGame() {
        // ARRANGE
        Game testGame1 = new Game();
        Game testGameToDelete = new Game();
        User userExpected = new User(userEmail)
                .withGame(testGame1);

        User testUser = userExpected.withGame(testGameToDelete);

        when(userRepo.save(userExpected)).thenReturn(userExpected);

        // ACT
        User userActual = serviceUnderTest.removeGame(testUser, testGameToDelete);

        // ASSERT
        assertEquals(userExpected, userActual);
        verify(userRepo).save(userExpected);
        verifyNoMoreInteractions(userRepo);
    }
}