package in.kahl.promptwhispers.security;

import in.kahl.promptwhispers.model.User;
import in.kahl.promptwhispers.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class SecurityConfigTest {
    private final UserRepo userRepo = mock(UserRepo.class);

    private SecurityConfig underTest;

    @BeforeEach
    void setUp() {
        underTest = new SecurityConfig(userRepo);
    }

    @Test
    void saveNewUserTest_whenUserNotExists_thenSaveNewUser() {
        // ARRANGE
        String emailAddress = "user@example.com";
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn(emailAddress);
        when(userRepo.existsByEmail(emailAddress)).thenReturn(false);

        User testUser = new User(emailAddress);
        when(userRepo.save(testUser)).thenReturn(testUser);

        // ACT
        Boolean actual = underTest.saveNewUser(oauth2User);

        // ASSERT
        assertTrue(actual);
        verify(userRepo).existsByEmail(emailAddress);
        verify(userRepo).save(any(User.class));
        verifyNoMoreInteractions(userRepo);
    }

    @Test
    void saveNewUserTest_whenUserExists_thenReturnTrue() {
        // ARRANGE
        String emailAddress = "user@example.com";
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn(emailAddress);
        when(userRepo.existsByEmail(emailAddress)).thenReturn(true);

        // ACT
        Boolean actual = underTest.saveNewUser(oauth2User);

        // ASSERT
        assertTrue(actual);
        verify(userRepo).existsByEmail(emailAddress);
        verifyNoMoreInteractions(userRepo);
    }

    @Test
    void saveNewUserTest_whenOAuthUserHasNoEmail_thenReturnFalse() {
        // ARRANGE
        String emailAddress = "";
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn(emailAddress);

        // ACT
        Boolean actual = underTest.saveNewUser(oauth2User);

        // ASSERT
        assertFalse(actual);
    }
}