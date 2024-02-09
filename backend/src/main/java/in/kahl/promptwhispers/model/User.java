package in.kahl.promptwhispers.model;


import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.UUID;

public record User(
        @Id
        String id,
        String email,
        Boolean isGoogle,
        Instant createdAt
) {
    public User(String email, Boolean isGoogle) {
        this(UUID.randomUUID().toString(), email, isGoogle, Instant.now());
    }
}

