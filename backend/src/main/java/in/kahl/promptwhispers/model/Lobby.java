package in.kahl.promptwhispers.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.List;
import java.util.UUID;

public record Lobby(
        @Id
        String id,
        @DBRef
        User host,
        @DBRef
        List<User> players,
        boolean isGameStarted,
        boolean isGameFinished
) {
    public Lobby(User hostUser) {
        this(UUID.randomUUID().toString(), hostUser, List.of(hostUser), false, false);
    }
}
