package in.kahl.promptwhispers.integration;

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
        mockMvc.perform(post("/api/play/start")
                        .contentType(MediaType.APPLICATION_JSON))

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
        String saveResult = mockMvc.perform(post("/api/play/start")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String gameId = JsonPath.parse(saveResult).read("$.id");
        // ACT
        mockMvc.perform(MockMvcRequestBuilders.get("/api/play/" + gameId)
                        .contentType(MediaType.APPLICATION_JSON))
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
        String saveResult = mockMvc.perform(post("/api/play/start")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String gameId = JsonPath.parse(saveResult).read("$.id");

        // ACT
        mockMvc.perform(post("/api/play/" + gameId + "/prompt")
                        .contentType(MediaType.APPLICATION_JSON)
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
