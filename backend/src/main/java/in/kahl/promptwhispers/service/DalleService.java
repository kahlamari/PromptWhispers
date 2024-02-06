package in.kahl.promptwhispers.service;

import in.kahl.promptwhispers.model.openai.DalleRequest;
import in.kahl.promptwhispers.model.openai.DalleResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

public class DalleService {
    private final RestClient restClient;

    public DalleService(@Value("${app.dalle.api.url}") String url,
                        @Value("${app.openai.api.key}") String key,
                        @Value("${app.openai.api.org}") String org) {
        this.restClient = RestClient.builder()
                .baseUrl(url)
                .defaultHeader("Authorization", "Bearer " + key)
                .defaultHeader("OpenAI-Organization", org)
                .build();
    }

    public String getGeneratedImage(String prompt) {
        DalleResponse response = restClient.post()
                .uri("/generations")
                .body(new DalleRequest(prompt))
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(DalleResponse.class);

        return response.data().getFirst().url();
    }
}
