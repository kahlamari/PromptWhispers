package in.kahl.promptwhispers.model;

import in.kahl.promptwhispers.Renderable;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.UUID;

public record Prompt(
        @Id
        String id,
        String prompt,
        Instant createdAt
) implements Renderable {
    public Prompt(String prompt) {
        this(UUID.randomUUID().toString(), prompt, Instant.now());
    }

    @Override
    public String render() {
        return prompt();
    }
}
