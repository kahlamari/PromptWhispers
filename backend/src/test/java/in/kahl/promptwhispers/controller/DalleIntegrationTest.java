package in.kahl.promptwhispers.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.kahl.promptwhispers.model.Game;
import in.kahl.promptwhispers.model.Turn;
import in.kahl.promptwhispers.model.User;
import in.kahl.promptwhispers.model.dto.RoundResponse;
import in.kahl.promptwhispers.repo.UserRepo;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DalleIntegrationTest {
    private static MockWebServer mockWebServer;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Cloudinary cloudinary;

    private final String userEmail = "user@example.com";

    @Autowired
    private UserRepo userRepo;

    @DynamicPropertySource
    public static void configureUrl(DynamicPropertyRegistry registry) {
        registry.add("app.dalle.api.url", () -> mockWebServer.url("/").toString());
        registry.add("app.openai.api.key", () -> mockWebServer.url("/").toString());
        registry.add("app.openai.api.org", () -> mockWebServer.url("/").toString());
    }

    @BeforeAll
    public static void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    public static void cleanup() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        User user = new User(userEmail);
        userRepo.save(user);
    }

    @Test
    @DirtiesContext
    void generateImageTest_whenRequested_thenReturnGeneratedImageUrl() throws Exception {
        // ARRANGE
        String saveResult = mockMvc.perform(post("/api/games/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Game game = objectMapper.readValue(saveResult, Game.class);

        saveResult = mockMvc.perform(post("/api/games/" + game.id() + "/prompt")
                .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", userEmail)))
                .content("""
                           {"prompt": "Goat jumps over a hedge."}
                        """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        game = objectMapper.readValue(saveResult, Game.class);

        mockWebServer.enqueue(new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody("""
                        {
                             "created": 1707227208,
                             "data": [
                                 {
                                     "url": "https://example.com/image.png"
                                 }
                             ]
                        }
                        """));

        String imageUrl = "https://example.com/image.png";
        Map<String, Object> mockResponse = Map.of("secure_url", imageUrl);

        when(cloudinary.uploader()).thenReturn(mock(Uploader.class));
        when(cloudinary.uploader().upload(anyString(), anyMap())).thenReturn(mockResponse);

        // ACT
        String resultJSON = mockMvc.perform(post("/api/games/" + game.id() + "/generateImage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(oidcLogin().userInfoToken(token -> token.claim("email", "user@example.com"))))
                // ASSERT
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.turns").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.isFinished", is(false)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        RoundResponse gameActual = objectMapper.readValue(resultJSON, RoundResponse.class);
        Turn imageTurn = gameActual.turns().getLast();

        assertEquals(game.id(), gameActual.gameId());
        assertEquals(imageUrl, imageTurn.content());
        assertTrue(Instant.now().minusSeconds(10L).isBefore(imageTurn.createdAt()));
    }
}
