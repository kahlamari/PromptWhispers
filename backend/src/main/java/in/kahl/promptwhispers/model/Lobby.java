package in.kahl.promptwhispers.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record Lobby(
        @Id
        String id,
        @DBRef
        User host,
        @DBRef
        List<User> players,
        String gameId,
        boolean isGameStarted,
        boolean isGameFinished,
        Instant createdAt
) {
    public Lobby(User hostUser) {
        this(UUID.randomUUID().toString(), hostUser, new ArrayList<>(List.of(hostUser)), null, false, false,
                Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }

    public Lobby withGameId(String gameId) {
        return new Lobby(id(), host(), players(), gameId, true, isGameFinished(), createdAt());
    }

    public Lobby withPlayer(User player) {
        players().add(player);
        return new Lobby(id(),
                host(),
                players(),
                gameId(),
                isGameStarted(),
                isGameFinished(),
                createdAt());
    }
}
