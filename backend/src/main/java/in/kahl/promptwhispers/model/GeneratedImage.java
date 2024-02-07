package in.kahl.promptwhispers.model;

import in.kahl.promptwhispers.Renderable;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.UUID;

public record GeneratedImage(
        @Id
        String id,
        String imageUrl,
        Instant createdAt
) implements Renderable {

    public GeneratedImage(String imageUrl) {
        this(UUID.randomUUID().toString(), imageUrl, Instant.now());
    }
    @Override
    public String render() {
        return imageUrl();
    }
}
