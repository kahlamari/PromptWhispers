package in.kahl.promptwhispers.model;


import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.UUID;

public record User(
        @Id
        String id,
        String email,
        AuthProvider authProvider,
        Instant createdAt
) {
    public User(String email) {
        this(UUID.randomUUID().toString(), email, AuthProvider.GOOGLE, Instant.now());
    }
}

