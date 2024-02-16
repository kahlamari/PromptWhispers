package in.kahl.promptwhispers.model;


import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public record User(
        @Id
        String id,
        String email,
        List<String> gameIds,
        AuthProvider authProvider,
        Instant createdAt
) {
    public User(String email) {
        this(UUID.randomUUID().toString(), email, Collections.emptyList(), AuthProvider.GOOGLE, Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }

    public User withGames(List<String> gamesList) {
        return new User(id(), email(), gamesList, authProvider(), createdAt());
    }

    public User withGame(Game game) {
        List<String> updatedGames = new LinkedList<>(gameIds());
        updatedGames.add(game.id());
        return withGames(updatedGames);
    }

    public User withoutGame(Game game) {
        List<String> updatedGames = new ArrayList<>(gameIds());
        updatedGames.remove(game.id());
        return withGames(updatedGames);
    }
}

