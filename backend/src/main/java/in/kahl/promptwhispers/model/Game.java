package in.kahl.promptwhispers.model;

import in.kahl.promptwhispers.model.dto.RoundResponse;
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
                gameState());
    }

    public RoundResponse asRoundResponse(User player) {
        int playerIndex = players().indexOf(player);
        int imageTurns = getNumOfCompletedImageTurns();

        int roundOfPlayer = (playerIndex + imageTurns) % players().size();

        return new RoundResponse(
                id(),
                rounds().get(roundOfPlayer) == null ? Collections.emptyList() : rounds().get(roundOfPlayer),
                gameState());
    }

    public Game withPlayer(User player) {
        if (!players().contains(player)) {
            players().add(player);
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

        for (List<Turn> turns : rounds.values()) {
            minTurnNumber = Math.min(minTurnNumber, (int) turns.stream().filter(turn -> turn.type().equals(TurnType.IMAGE)).count());
        }

        return minTurnNumber;
    }

    private boolean isGameFinished() {
        return getNumOfCompletedImageTurns() >= players().size();
    }

    private boolean haveAllRoundsSameNumberOfTurnsByType(TurnType turnType) {
        Set<Long> counts = rounds().values().stream()
                .map(turns -> turns.stream().filter(turn -> turn.type().equals(turnType)).count())
                .collect(Collectors.toSet());

        if (players().size() != rounds().size()) {
            return counts.contains(0L);
        }
        return counts.size() == 1;
    }

    private GameState determineGameState() {
        System.out.println("Game State: " + gameState());
        System.out.println("Prompt same: " + haveAllRoundsSameNumberOfTurnsByType(TurnType.PROMPT));
        System.out.println("Image same: " + haveAllRoundsSameNumberOfTurnsByType(TurnType.IMAGE));
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
        int playerIndex = players().indexOf(turn.player());

        int offset = (playerIndex + getNumOfCompletedImageTurns()) % players().size();

        List<Turn> updatedTurns = new ArrayList<>();
        if (rounds().get(offset) != null) {
            updatedTurns.addAll(rounds().get(offset));
        }
        updatedTurns.add(turn);

        rounds().put(offset, updatedTurns);

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
