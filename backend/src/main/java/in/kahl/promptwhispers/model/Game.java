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

    public RoundResponse asRoundResponse(User player) {
        int playerIndex = players().indexOf(player);
        int imageTurns = getLastCompletedImageTurn();

        int roundOfPlayer = (playerIndex + imageTurns) % players().size();

        return new RoundResponse(
                id(),
                rounds().get(roundOfPlayer) == null ? Collections.emptyList() : rounds().get(roundOfPlayer),
                gameState() == GameState.FINISHED
        );
    }

    public Game withPlayer(User player) {
        if (!players().contains(player)) {
            players().add(player);
        }

        return new Game(id(), players(), rounds(), gameState(), createdAt());
    }

    public int getLastCompletedImageTurn() {
        int minTurnNumber = players().size();

        for (List<Turn> turns : rounds.values()) {
            minTurnNumber = Math.min(minTurnNumber, (int) turns.stream().filter(turn -> turn.type().equals(TurnType.IMAGE)).count());
        }

        return minTurnNumber;
    }

    public int getLastCompletedTurn() {
        int minTurnNumber = players().size() * 2;
        for (List<Turn> turns : rounds().values()) {
            minTurnNumber = Math.min(minTurnNumber, turns.size());
        }

        return minTurnNumber;
    }

    public boolean turnCompleted() {
        int minTurnNumber = getLastCompletedTurn();
        return rounds().values().stream().allMatch(steps -> steps.size() == minTurnNumber);
    }

    public Game withTurn(Turn turn) {
        int playerIndex = players().indexOf(turn.player());

        // first step
        int offset = (playerIndex + getLastCompletedImageTurn()) % players().size();
        System.out.println("offset: " + offset);

        List<Turn> updatedTurns = new ArrayList<>();
        if (rounds().get(offset) != null) {
            updatedTurns.addAll(rounds().get(offset));
        }
        updatedTurns.add(turn);

        rounds().put(offset, updatedTurns);

        return new Game(id(),
                players(),
                rounds(),
                gameState(),
                createdAt());
    }
}
