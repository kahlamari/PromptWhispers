package in.kahl.promptwhispers.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import in.kahl.promptwhispers.model.Game;
import in.kahl.promptwhispers.model.GameState;
import in.kahl.promptwhispers.model.Lobby;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        User host = userRepo.getUserByEmail(userEmail);
        Lobby lobby = new Lobby(host);
        String lobbyAsJSON = objectMapper.writeValueAsString(lobby);

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", host.email())))
                        .content(lobbyAsJSON))
                // ASSERT
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.rounds").isArray())
                .andExpect(jsonPath("$.rounds[0]").isEmpty())
                .andExpect(jsonPath("$.gameState", is(GameState.REQUEST_NEW_PROMPTS.toString())));
    }

    @Test
    @DirtiesContext
    void getGameTest_whenGameExists_thenReturnGame() throws Exception {
        // ARRANGE
        User host = userRepo.getUserByEmail(userEmail);
        Lobby lobby = new Lobby(host);
        String lobbyAsJSON = objectMapper.writeValueAsString(lobby);

        String saveArrangeResult = mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", host.email())))
                        .content(lobbyAsJSON))
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
                .andExpect(jsonPath("$.rounds").isArray())
                .andExpect(jsonPath("$.rounds[0]").isEmpty())
                .andExpect(jsonPath("$.gameState", is(GameState.REQUEST_NEW_PROMPTS.toString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Game game = objectMapper.readValue(saveResult, Game.class);
        assertEquals(gameExpected.id(), game.id());
        assertEquals(gameExpected.rounds(), game.rounds());
        assertEquals(gameExpected.gameState(), game.gameState());
    }

    @Test
    @DirtiesContext
    void getGameTest_whenGameNotExists_thenThrowException() throws Exception {
        // ACT
        mockMvc.perform(get("/api/games/not_existent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))

                // ASSERT
                .andExpect(status().isNotFound())
                .andExpect(content().json("""
                        {"message":"NoSuchElementException: The game associated with your request does not exist."}
                        """));
    }

    @Test
    @DirtiesContext
    void getAllGamesTest_whenRequestGames_thenReturnGames() throws Exception {
        // ARRANGE
        User host = userRepo.getUserByEmail(userEmail);
        User bob = new User("bob@example.com");
        userRepo.save(bob);
        Lobby lobby1 = new Lobby(host).withPlayer(bob);
        String lobby1AsJSON = objectMapper.writeValueAsString(lobby1);
        Lobby lobby2 = new Lobby(host).withPlayer(bob);
        String lobby2AsJSON = objectMapper.writeValueAsString(lobby2);


        String game1Result = mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", host.email())))
                        .content(lobby1AsJSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Game game1 = objectMapper.readValue(game1Result, Game.class);

        String game2Result = mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", host.email())))
                        .content(lobby2AsJSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

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

        List<Game> gameListActual = objectMapper.readValue(gameListResult, new TypeReference<>() {
        });

        List<String> gameIdListActual = gameListActual.stream().map(Game::id).toList();

        assertEquals(List.of(game1.id(), game2.id()), gameIdListActual);
    }

    @Test
    @DirtiesContext
    void deleteGameTest_whenGameExists_thenRemoveGame() throws Exception {
        // ARRANGE
        User host = userRepo.getUserByEmail(userEmail);
        Lobby lobby = new Lobby(host);
        String lobbyAsJSON = objectMapper.writeValueAsString(lobby);

        String game1Result = mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", host.email())))
                        .content(lobbyAsJSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Game game1 = objectMapper.readValue(game1Result, Game.class);

        // ACT
        mockMvc.perform(delete("/api/games/" + game1.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))

                // ASSERT
                .andExpect(status().isNoContent());

        List<String> gameListActual = userRepo.getUserByEmail(userEmail).gameIds();

        assertEquals(0, gameListActual.size());
    }

    @Test
    @DirtiesContext
    void deleteGameTest_whenUserNotOwner_thenThrowException() throws Exception {
        // ARRANGE
        User host = userRepo.getUserByEmail(userEmail);
        User bob = userRepo.save(new User("bob@example.com"));
        Lobby lobby = new Lobby(host);
        lobby = lobby.withPlayer(bob);
        String lobbyAsJSON = objectMapper.writeValueAsString(lobby);

        String game1Result = mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail)))
                        .content(lobbyAsJSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Game game1 = objectMapper.readValue(game1Result, Game.class);

        User userWithoutGame = new User("2" + userEmail);
        userRepo.save(userWithoutGame);

        // ACT
        mockMvc.perform(delete("/api/games/" + game1.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userWithoutGame.email()))))

                // ASSERT
                .andExpect(status().isForbidden())
                .andExpect(content().json("""
                        {"message":"AccessDeniedException: You are not allowed to delete this game."}
                        """));
    }

    @Test
    @DirtiesContext
    void submitPromptTest_whenSubmitPrompt_thenReturnRoundWithPrompt() throws Exception {
        // ARRANGE
        User host = userRepo.getUserByEmail(userEmail);
        User bob = userRepo.save(new User("bob@example.com"));
        Lobby lobby = new Lobby(host);
        lobby = lobby.withPlayer(bob);
        String lobbyAsJSON = objectMapper.writeValueAsString(lobby);

        String saveResult = mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail)))
                        .content(lobbyAsJSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String gameId = JsonPath.parse(saveResult).read("$.id");

        // ACT
        MvcResult result = mockMvc.perform(post("/api/games/" + gameId + "/prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail)))
                        .content("""
                                   {"prompt": "Goat jumps over a hedge."}
                                """))
                // ASSERT
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.rounds").isArray())
                .andExpect(jsonPath("$.rounds[0]").isNotEmpty())
                .andExpect(jsonPath("$.gameState", is(GameState.WAIT_FOR_PROMPTS.toString())))
                .andReturn();
    }
}
