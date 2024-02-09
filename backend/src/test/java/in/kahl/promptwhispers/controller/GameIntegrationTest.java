package in.kahl.promptwhispers.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DirtiesContext
    void createGameTest_whenCalled_thenReturnGame() throws Exception {
        // ACT
        mockMvc.perform(post("/api/games/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", "user@example.com"))))

                // ASSERT
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.steps").isEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.isFinished", is(false)));
    }

    @Test
    @DirtiesContext
    void getGameTest_whenGameExists_thenReturnGame() throws Exception {
        // ARRANGE
        String saveResult = mockMvc.perform(post("/api/games/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", "user@example.com"))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String gameId = JsonPath.parse(saveResult).read("$.id");
        // ACT
        mockMvc.perform(MockMvcRequestBuilders.get("/api/games/" + gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", "user@example.com"))))
                // ASSERT
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.steps").isEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.isFinished", is(false)));
    }

    @Test
    @DirtiesContext
    void submitPromptTest_whenPromptSubmitted_thenPromptSaved() throws Exception {
        // ARRANGE
        String saveResult = mockMvc.perform(post("/api/games/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", "user@example.com"))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String gameId = JsonPath.parse(saveResult).read("$.id");

        // ACT
        mockMvc.perform(post("/api/games/" + gameId + "/prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("login", "test-user")))
                        .content("""
                                   {"prompt": "Goat jumps over a hedge."}
                                """))
                // ASSERT
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.steps").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.isFinished", is(false)));
    }
}
