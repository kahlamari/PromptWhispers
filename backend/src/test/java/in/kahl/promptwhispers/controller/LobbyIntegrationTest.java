package in.kahl.promptwhispers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.kahl.promptwhispers.model.Lobby;
import in.kahl.promptwhispers.model.User;
import in.kahl.promptwhispers.repo.LobbyRepo;
import in.kahl.promptwhispers.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LobbyIntegrationTest {

    private final String userEmail = "user@example.com";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private LobbyRepo lobbyRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        User user = new User(userEmail);
        userRepo.save(user);
    }

    @Test
    @DirtiesContext
    void createLobbyTest_whenCalled_thenReturnLobby() throws Exception {
        // ACT
        mockMvc.perform(post("/api/lobbies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))

                // ASSERT
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.host").isNotEmpty())
                .andExpect(jsonPath("$.players").isNotEmpty())
                .andExpect(jsonPath("$.isGameStarted", is(false)))
                .andExpect(jsonPath("$.isGameFinished", is(false)))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @DirtiesContext
    void getLobbyByIdTest_whenLobbyExists_thenReturnLobby() throws Exception {
        // ARRANGE
        String saveArrangeResult = mockMvc.perform(post("/api/lobbies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Lobby lobbyExpected = objectMapper.readValue(saveArrangeResult, Lobby.class);

        // ACT
        String saveResult = mockMvc.perform(get("/api/lobbies/" + lobbyExpected.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))

                // ASSERT
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.host").isNotEmpty())
                .andExpect(jsonPath("$.players").isNotEmpty())
                .andExpect(jsonPath("$.isGameStarted", is(false)))
                .andExpect(jsonPath("$.isGameFinished", is(false)))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Lobby lobbyActual = objectMapper.readValue(saveResult, Lobby.class);
        assertEquals(lobbyExpected, lobbyActual);
    }

    @Test
    @DirtiesContext
    void getLobbyByIdTest_whenLobbyNotExists_thenThrowException() throws Exception {
        // ACT
        mockMvc.perform(get("/api/lobbies/not_existent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))

                // ASSERT
                .andExpect(status().isNotFound())
                .andExpect(content().json("""
                        {"message":"NoSuchElementException: The lobby of your request does not exist."}
                        """));
    }

    @Test
    @DirtiesContext
    void joinLobbyTest_whenRequestToJoin_returnLobbyWithPlayer() throws Exception {
        // ARRANGE
        String saveArrangeResult = mockMvc.perform(post("/api/lobbies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Lobby lobby = objectMapper.readValue(saveArrangeResult, Lobby.class);

        User playerToJoin = new User("join" + userEmail);
        userRepo.save(playerToJoin);

        // ACT
        String lobbyJSON = mockMvc.perform(put("/api/lobbies/" + lobby.id() + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", playerToJoin.email()))))

                // ASSERT
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Lobby lobbyActual = objectMapper.readValue(lobbyJSON, Lobby.class);
        assertTrue(lobbyActual.players().contains(playerToJoin));
    }

    @Test
    @DirtiesContext
    void leaveLobbyTest_whenRequestToLeave_returnLobbyWithoutPlayer() throws Exception {
        // ARRANGE
        String saveArrangeResult = mockMvc.perform(post("/api/lobbies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Lobby lobby = objectMapper.readValue(saveArrangeResult, Lobby.class);

        User playerToLeave = new User("leave" + userEmail);
        userRepo.save(playerToLeave);

        String lobbyWithPlayerJSON = mockMvc.perform(put("/api/lobbies/" + lobby.id() + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", playerToLeave.email()))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Lobby lobbyWithPlayer = objectMapper.readValue(lobbyWithPlayerJSON, Lobby.class);

        // just to ensure that the player has been in the players list
        assertTrue(lobbyWithPlayer.players().contains(playerToLeave));

        // ACT
        String lobbyJSON = mockMvc.perform(put("/api/lobbies/" + lobby.id() + "/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", playerToLeave.email()))))

                // ASSERT
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Lobby lobbyActual = objectMapper.readValue(lobbyJSON, Lobby.class);
        assertFalse(lobbyActual.players().contains(playerToLeave));
    }

    @Test
    @DirtiesContext
    void leaveLobbyTest_whenLobbyHostRequestsLeave_thenThrowException() throws Exception {
        // ARRANGE
        String saveArrangeResult = mockMvc.perform(post("/api/lobbies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Lobby lobby = objectMapper.readValue(saveArrangeResult, Lobby.class);

        // ACT
        mockMvc.perform(put("/api/lobbies/" + lobby.id() + "/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))

                // ASSERT
                .andExpect(status().isForbidden())
                .andExpect(content().json("""
                        {"message":"AccessDeniedException: You cannot leave the lobby when you are the host."}
                        """));
    }

    @Test
    @DirtiesContext
    void deleteLobbyTest_whenUserIsHost_thenRemoveLobby() throws Exception {
        // ARRANGE
        String saveArrangeResult = mockMvc.perform(post("/api/lobbies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Lobby lobby = objectMapper.readValue(saveArrangeResult, Lobby.class);

        // Ensuring that there's a Lobby object in the DB
        assertEquals(1, lobbyRepo.count());

        // ACT
        mockMvc.perform(delete("/api/lobbies/" + lobby.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))

                // ASSERT
                .andExpect(status().isNoContent());

        assertEquals(0, lobbyRepo.count());
    }

    @Test
    @DirtiesContext
    void deleteLobbyTest_whenUserIsNotHost_thenThrowException() throws Exception {
        // ARRANGE
        String saveArrangeResult = mockMvc.perform(post("/api/lobbies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Lobby lobby = objectMapper.readValue(saveArrangeResult, Lobby.class);

        // Ensuring that there's a Lobby object in the DB
        assertEquals(1, lobbyRepo.count());

        // adding user that is not the host of the lobby
        User notHost = userRepo.save(new User("not_host" + userEmail));

        // ACT
        mockMvc.perform(delete("/api/lobbies/" + lobby.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", notHost.email()))))
                .andExpect(status().isForbidden())
                .andExpect(content().json("""
                        {"message":"AccessDeniedException: You are not allowed to delete this lobby."}
                        """));
    }
}
