package in.kahl.promptwhispers.service;

import in.kahl.promptwhispers.model.User;
import in.kahl.promptwhispers.model.dto.UserResponse;
import in.kahl.promptwhispers.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private final UserRepo userRepo = mock(UserRepo.class);
    private UserService serviceUnderTest;

    @BeforeEach
    void setUp() {
        serviceUnderTest = new UserService(userRepo);
    }

    @Test
    void getLoggedInUserTest_whenProvideOAuthUser_thenReturnUserResponse() {
        // ARRANGE
        OAuth2User oAuth2User = mock(OAuth2User.class);
        String userEmail = "user@example.com";
        when(oAuth2User.getAttribute("email")).thenReturn(userEmail);

        User user = new User(userEmail);
        when(userRepo.getUserByEmail(userEmail)).thenReturn(user);
        UserResponse userResponseExpected = new UserResponse(user);

        // ACT
        UserResponse userResponseActual = serviceUnderTest.getLoggedInUser(oAuth2User);

        // ASSERT
        assertEquals(userResponseExpected, userResponseActual);
        verify(userRepo).getUserByEmail(userEmail);
        verifyNoMoreInteractions(userRepo);
    }

    @Test
    void getLoggedInUserTest_whenProvideNull_thenReturnNull() {
        // ARRANGE & ACT
        UserResponse userResponseActual = serviceUnderTest.getLoggedInUser(null);

        // ASSERT
        assertNull(userResponseActual);
    }

    @Test
    void getLoggedInUserTest_whenEmptyEmail_thenReturnNull() {
        // ARRANGE
        OAuth2User oAuth2User = mock(OAuth2User.class);
        String userEmail = "";
        when(oAuth2User.getAttribute("email")).thenReturn(userEmail);

        // ACT
        UserResponse userResponseActual = serviceUnderTest.getLoggedInUser(oAuth2User);

        // ASSERT
        assertNull(userResponseActual);
    }

    @Test
    void getLoggedInUserTest_whenEmailNull_thenReturnNull() {
        // ARRANGE
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn(null);

        // ACT
        UserResponse userResponseActual = serviceUnderTest.getLoggedInUser(oAuth2User);

        // ASSERT
        assertNull(userResponseActual);
    }

    @Test
    void getLoggedInUserTest_whenUserNotInDB_thenReturnNull() {
        // ARRANGE
        OAuth2User oAuth2User = mock(OAuth2User.class);
        String userEmail = "user@example.com";
        when(oAuth2User.getAttribute("email")).thenReturn(userEmail);
        when(userRepo.getUserByEmail(userEmail)).thenReturn(null);

        // ACT
        UserResponse userResponseActual = serviceUnderTest.getLoggedInUser(oAuth2User);

        // ASSERT
        assertNull(userResponseActual);
    }
}