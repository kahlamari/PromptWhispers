package in.kahl.promptwhispers.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import in.kahl.promptwhispers.model.Game;
import in.kahl.promptwhispers.model.User;
import in.kahl.promptwhispers.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private final String userEmail = "user@example.com";
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        User user = new User(userEmail);
        userRepo.save(user);
    }

    @Test
    @DirtiesContext
    void createGameTest_whenCalled_thenReturnGame() throws Exception {
        // ACT
        mockMvc.perform(post("/api/games/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))

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
        String saveArrangeResult = mockMvc.perform(post("/api/games/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Game gameExpected = objectMapper.readValue(saveArrangeResult, Game.class);

        // ACT
        String saveResult = mockMvc.perform(get("/api/games/" + gameExpected.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))

                // ASSERT
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.steps").isEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.isFinished", is(false)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Game game = objectMapper.readValue(saveResult, Game.class);
        assertEquals(gameExpected.id(), game.id());
        assertEquals(gameExpected.steps(), game.steps());
        assertEquals(gameExpected.isFinished(), game.isFinished());
    }

    @Test
    @DirtiesContext
    void getAllGamesTest_whenRequestGames_thenReturnGames() throws Exception {
        // ARRANGE
        String game1Result = mockMvc.perform(post("/api/games/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String game2Result = mockMvc.perform(post("/api/games/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Game game1 = objectMapper.readValue(game1Result, Game.class);
        Game game2 = objectMapper.readValue(game2Result, Game.class);

        // ACT
        String gameListResult = mockMvc.perform(get("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))

                // ASSERT
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<Game> gameListActual = objectMapper.readValue(gameListResult, new TypeReference<List<Game>>() {
        });

        assertEquals(List.of(game1, game2), gameListActual);
    }

    @Test
    @DirtiesContext
    void deleteGameTest_whenGameExists_thenRemoveGame() throws Exception {
        // ARRANGE
        String game1Result = mockMvc.perform(post("/api/games/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String game2Result = mockMvc.perform(post("/api/games/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Game game1 = objectMapper.readValue(game1Result, Game.class);
        Game game2 = objectMapper.readValue(game2Result, Game.class);

        // ACT
        mockMvc.perform(delete("/api/games/" + game1.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))

                // ASSERT
                .andExpect(status().isNoContent());

        List<Game> gameListActual = userRepo.getUserByEmail(userEmail).games();

        assertEquals(List.of(game2), gameListActual);
        assertEquals(1, gameListActual.size());
    }

    @Test
    @DirtiesContext
    void submitPromptTest_whenPromptSubmitted_thenPromptSaved() throws Exception {
        // ARRANGE
        String saveResult = mockMvc.perform(post("/api/games/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String gameId = JsonPath.parse(saveResult).read("$.id");

        // ACT
        mockMvc.perform(post("/api/games/" + gameId + "/prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail)))
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
