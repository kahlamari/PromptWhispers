package in.kahl.promptwhispers.model;


import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record User(
        @Id
        String id,
        String email,
        String profilePicUrl,
        List<String> gameIds,
        AuthProvider authProvider,
        Instant createdAt
) {
    public User(String email) {
        this(email, "");
    }

    public User(String email, String profilePicUrl) {
        this(UUID.randomUUID().toString(), email, profilePicUrl, new ArrayList<>(Collections.emptyList()), AuthProvider.GOOGLE, Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }

    public User withGameIds(List<String> gamesList) {
        return new User(id(), email(), profilePicUrl(), gamesList, authProvider(), createdAt());
    }

    public User withGame(Game game) {
        List<String> updatedGames = new ArrayList<>();
        if (gameIds() != null) {
            updatedGames.addAll(gameIds());
        }
        updatedGames.add(game.id());
        return withGameIds(updatedGames);
    }

    public User withGameId(String gameId) {
        List<String> updatedGameIds = new ArrayList<>();
        if (gameIds() != null) {
            updatedGameIds.addAll(gameIds());
        }
        updatedGameIds.add(gameId);
        return withGameIds(updatedGameIds);
    }

    public User withoutGame(Game game) {
        List<String> updatedGames = new ArrayList<>(gameIds());
        updatedGames.remove(game.id());
        return withGameIds(updatedGames);
    }
}

