package in.kahl.promptwhispers.model;

import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.UUID;

public record Turn(
        @Id
        String id,
        TurnType type,
        String content,
        Instant createdAt
) {
    public Turn(TurnType type, String content) {
        this(UUID.randomUUID().toString(), type, content, Instant.now());
    }
}
