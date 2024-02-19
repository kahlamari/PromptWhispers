package in.kahl.promptwhispers.model;

import in.kahl.promptwhispers.model.dto.RoundResponse;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public record Game(
        @Id
        String id,
        @DBRef
        List<User> players,
        Map<Integer, List<Turn>> rounds,
        GameState gameState,
        Instant createdAt
) {
    public Game(User host) {
        this(UUID.randomUUID().toString(),
                new ArrayList<>(List.of(host)),
                Collections.emptyMap(),
                GameState.NEW,
                Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }

    public RoundResponse asRoundResponse() {
        int playerIndex = 0;

        return new RoundResponse(
                id(),
                rounds().get(playerIndex) == null ? Collections.emptyList() : rounds().get(playerIndex),
                gameState() == GameState.FINISHED
        );
    }

    public int getLastCompletedTurn() {
        int minStepNumber = players().size() * 2;
        for (List<Turn> steps : rounds().values()) {
            minStepNumber = Math.min(minStepNumber, steps.size());
        }

        return minStepNumber;
    }

    public boolean turnCompleted() {
        int minTurnNumber = getLastCompletedTurn();
        return rounds().values().stream().allMatch(steps -> steps.size() == minTurnNumber);
    }

    public Game withTurn(Turn turn) {
        int playerIndex = players().indexOf(turn.player());

        // first step
        int offset = playerIndex + getLastCompletedTurn();

        ArrayList<Turn> updatedSteps = new ArrayList<>(rounds().get(offset));
        updatedSteps.add(turn);

        rounds().put(offset, updatedSteps);

        return new Game(id(),
                players(),
                rounds(),
                gameState(),
                createdAt());
    }
}
