package in.kahl.promptwhispers.model;

import in.kahl.promptwhispers.model.dto.GameResponse;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

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

    public GameResponse asGameResponse() {
        int playerIndex = 0;

        return new GameResponse(
                id(),
                rounds().get(playerIndex) == null ? Collections.emptyList() : rounds().get(playerIndex),
                createdAt(),
                gameState() == GameState.FINISHED
        );
    }

    public int getLastCompletedStep() {
        int minStepNumber = players().size() * 2;
        for (List<Step> steps : rounds().values()) {
            minStepNumber = Math.min(minStepNumber, steps.size());
        }

        return minStepNumber;
    }

    public boolean turnCompleted() {
        int minStepNumber = getLastCompletedStep();
        return rounds().values().stream().allMatch(steps -> steps.size() == minStepNumber);
    }

    public Game withTurn(Turn turn) {
        int playerIndex = players().indexOf(turn.player());

        // first step
        int offset = playerIndex + getLastCompletedStep();

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
