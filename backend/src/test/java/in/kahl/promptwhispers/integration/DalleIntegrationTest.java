package in.kahl.promptwhispers.integration;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.jayway.jsonpath.JsonPath;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.Map;
import java.time.Instant;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DalleIntegrationTest {
    private static MockWebServer mockWebServer;
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Cloudinary cloudinary;

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

    @Test
    @DirtiesContext
    void generateImageTest_whenRequested_thenReturnGeneratedImageUrl() throws Exception {
        // ARRANGE
        String saveResult = mockMvc.perform(post("/api/play/start")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String gameId = JsonPath.parse(saveResult).read("$.id");

        mockMvc.perform(post("/api/play/" + gameId + "/prompt")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                           {"prompt": "Goat jumps over a hedge."}
                        """));

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
        String resultJSON = mockMvc.perform(post("/api/play/" + gameId + "/generateImage")
                        .contentType(MediaType.APPLICATION_JSON))
                // ASSERT
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.steps").isMap())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.isFinished", is(false)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String gameIdActual = JsonPath.parse(resultJSON).read("$.id");
        assertEquals(gameId, gameIdActual);

        Map<String, Object> mapActual = JsonPath.parse(resultJSON).read("$.steps");
        assertEquals(2, mapActual.size());
        assertTrue(mapActual.get("1").toString().contains("imageUrl=https://example.com/image.png"));

        Instant instantActual = Instant.parse(JsonPath.parse(resultJSON).read("$.createdAt"));
        assertTrue(instantActual.isBefore(Instant.now()));
    }
}
