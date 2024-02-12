package in.kahl.promptwhispers.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public record User(
        @Id
        String id,
        String email,
        @DBRef
        List<Game> games,
        AuthProvider authProvider,
        Instant createdAt
) {
    public User(String email) {
        this(UUID.randomUUID().toString(), email, Collections.emptyList(), AuthProvider.GOOGLE, Instant.now());
    }

    public User withGame(Game game) {
        List<Game> updatedGames = new LinkedList<>(games());
        updatedGames.add(game);
        return new User(id(), email(), updatedGames, authProvider(), createdAt());
    }
}

