package in.kahl.promptwhispers.model;

import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.UUID;

public record Turn(
        @Id
        String id,
        @DBRef
        User player,
        TurnType type,
        String content,
        Instant createdAt
) {
    public Turn(TurnType type, String content) {
        this(null, type, content);
    }

    public Step(User player, StepType type, String content) {
        this(UUID.randomUUID().toString(), player, type, content, Instant.now());
    }
}
