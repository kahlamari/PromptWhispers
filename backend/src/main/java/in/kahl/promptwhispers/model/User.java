package in.kahl.promptwhispers.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.Instant;
import java.util.*;

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

    public User withGames(List<Game> gamesList) {
        return new User(id(), email(), gamesList, authProvider(), createdAt());
    }

    public User withGame(Game game) {
        List<Game> updatedGames = new LinkedList<>(games());
        updatedGames.add(game);
        return withGames(updatedGames);
    }

    public User withoutGame(Game game) {
        List<Game> updatedGames = new ArrayList<>(games());
        updatedGames.remove(game);
        return withGames(updatedGames);
    }
}

