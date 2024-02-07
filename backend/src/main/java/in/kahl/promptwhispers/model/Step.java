package in.kahl.promptwhispers.model;

import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.UUID;

public record Step(
        @Id
        String id,
        StepType type,
        String content,
        Instant createdAt
) {
    public Step(StepType type, String content) {
        this(UUID.randomUUID().toString(), type, content, Instant.now());
    }
}
