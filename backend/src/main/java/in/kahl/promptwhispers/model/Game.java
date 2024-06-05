package in.kahl.promptwhispers.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public record Game(
        @Id
        String id,
        @DBRef
        List<User> players,
        List<List<Turn>> rounds,
        GameState gameState,
        Instant createdAt
) {
    public Game() {
        this(UUID.randomUUID().toString(),
                new ArrayList<>(),
                new ArrayList<>(new ArrayList<>()),
                GameState.NEW,
                Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }

    public Game withPlayer(User player) {
        if (players().stream().noneMatch(p -> p.id().equals(player.id()))) {
            players().add(player);
            rounds().add(new ArrayList<>());
        }

        return new Game(id(), players(), rounds(), gameState(), createdAt());
    }

    public Game withGameState(GameState gameState) {
        return new Game(id(), players(), rounds(), gameState, createdAt());
    }

    private int getNumOfCompletedImageTurns() {
        int minTurnNumber = players().size();

        if (players().size() != rounds().size()) {
            return 0;
        }

        for (List<Turn> turns : rounds) {
            minTurnNumber = Math.min(minTurnNumber, (int) turns.stream().filter(turn -> turn.type().equals(TurnType.IMAGE)).count());
        }

        return minTurnNumber;
    }

    private boolean isGameFinished() {
        return gameState() == GameState.FINISHED || getNumOfCompletedImageTurns() >= players().size();
    }

    private boolean haveAllRoundsSameNumberOfTurnsByType(TurnType turnType) {
        Set<Long> counts = rounds().stream()
                .map(turns -> turns.stream().filter(turn -> turn.type().equals(turnType)).count())
                .collect(Collectors.toSet());

        if (players().size() != rounds().size()) {
            return counts.contains(0L);
        }
        return counts.size() == 1;
    }

    private GameState determineGameState() {
        if (isGameFinished()) {
            return GameState.FINISHED;
        }

        if (gameState().equals(GameState.NEW)) {
            return GameState.REQUEST_NEW_PROMPTS;
        }

        if (gameState().equals(GameState.REQUEST_NEW_PROMPTS)
                && !haveAllRoundsSameNumberOfTurnsByType(TurnType.PROMPT)
                && haveAllRoundsSameNumberOfTurnsByType(TurnType.IMAGE)) {
            return GameState.WAIT_FOR_PROMPTS;
        }

        if (gameState().equals(GameState.WAIT_FOR_PROMPTS)
                && haveAllRoundsSameNumberOfTurnsByType(TurnType.PROMPT)
                && !haveAllRoundsSameNumberOfTurnsByType(TurnType.IMAGE)) {
            return GameState.WAIT_FOR_IMAGES;
        }

        if (gameState().equals(GameState.WAIT_FOR_IMAGES)
                && haveAllRoundsSameNumberOfTurnsByType(TurnType.PROMPT)
                && haveAllRoundsSameNumberOfTurnsByType(TurnType.IMAGE)) {
            return GameState.REQUEST_NEW_PROMPTS;
        }
        return gameState();
    }

    public Game withTurn(Turn turn) {
        if (isGameFinished()) {
            throw new IllegalArgumentException("Game is finished. No turns can be added.");
        }

        int playerIndex = players().indexOf(turn.player());

        int offset = (playerIndex + getNumOfCompletedImageTurns()) % players().size();

        List<Turn> updatedTurns = new ArrayList<>();
        if (!rounds().isEmpty()) {
            updatedTurns.addAll(rounds().get(offset));
        }
        updatedTurns.add(turn);

        rounds().set(offset, updatedTurns);

        return new Game(id(),
                players(),
                rounds(),
                determineGameState(),
                createdAt());
    }

    public Turn getMostRecentPromptByPlayer(User player) {
        int playerIndex = players().indexOf(player);
        int offset = (playerIndex + getNumOfCompletedImageTurns()) % players().size();

        List<Turn> round = rounds().get(offset);

        if (round == null || round.isEmpty()) {
            throw new NoSuchElementException();
        }

        Turn turn = round.getLast();
        if (turn.type().equals(TurnType.PROMPT)) {
            return turn;
        }
        throw new NoSuchElementException();
    }
}
