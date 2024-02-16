package in.kahl.promptwhispers.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.Instant;
import java.util.UUID;

public record Step(
        @Id
        String id,
        @DBRef
        User player,
        StepType type,
        String content,
        Instant createdAt
) {
    public Step(StepType type, String content) {
        this(UUID.randomUUID().toString(), null, type, content, Instant.now());
    }

    public Step(User player, StepType type, String content) {
        this(UUID.randomUUID().toString(), player, type, content, Instant.now());
    }
}
