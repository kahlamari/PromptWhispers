package in.kahl.promptwhispers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.kahl.promptwhispers.model.User;
import in.kahl.promptwhispers.model.dto.UserResponse;
import in.kahl.promptwhispers.repo.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DirtiesContext
    void getUserTest_whenUserNotLoggedIn_thenReturnNull() throws Exception {
        // ARRANGE & ACT
        mockMvc.perform(get("/api/users"))
                // ASSERT
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @DirtiesContext
    void getUserTest_whenUserLoggedIn_thenReturnUserResponseDTO() throws Exception {
        // ARRANGE
        User testUser = new User("user@example.com");
        userRepo.save(testUser);

        OAuth2User oAuth2User = mock(OAuth2User.class);
        String userEmail = "user@example.com";
        when(oAuth2User.getAttribute("email")).thenReturn(userEmail);

        // ACT
        String resultJSON = mockMvc.perform(get("/api/users")
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse userResponseActual = objectMapper.readValue(resultJSON, UserResponse.class);
        UserResponse userResponseExpected = new UserResponse(userResponseActual.id(), userEmail);

        // ASSERT
        assertEquals(userResponseExpected, userResponseActual);
    }

    @Test
    @DirtiesContext
    void getUserTest_whenAuthTokenEmailIsNull_thenThrowException() throws Exception {
        // ARRANGE & ACT
        String result = mockMvc.perform(get("/api/users")
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", null))))
                // ASSERT
                .andExpect(status().isBadRequest())
                //.andExpect(content().json("""
                //        {"message":"GoogleEmailNotFoundException: Email must be present to proceed."}
                //        """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        System.out.println("next is the result");
        System.out.println(result);
    }

    @Test
    @DirtiesContext
    void getUserTest_whenAuthTokenEmailIsEmpty_thenThrowException() throws Exception {
        // ARRANGE & ACT
        mockMvc.perform(get("/api/users")
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", ""))))
                // ASSERT
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        {"message":"GoogleEmailNotFoundException: Email must be present to proceed."}
                        """));
    }
}
