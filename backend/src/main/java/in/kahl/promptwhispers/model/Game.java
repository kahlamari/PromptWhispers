package in.kahl.promptwhispers.model;

import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public record Game(
        @Id
        String id,
        List<Turn> turns,
        Instant createdAt,
        boolean isFinished
) {
    public Game() {
        this(UUID.randomUUID().toString(), Collections.emptyList(), Instant.now().truncatedTo(ChronoUnit.MILLIS), false);
    }

    public static final int MAX_IMAGE_STEPS = 3;

    public static boolean maxStepsReached(List<Turn> turns) {
        return turns.stream().filter(step -> step.type().equals(TurnType.IMAGE)).count() >= MAX_IMAGE_STEPS;
    }

    public Game withTurn(Turn turn) {
        if (isFinished()) {
            throw new IllegalArgumentException("Game is finished. Not steps can be added.");
        }
        List<Turn> turnList = new LinkedList<>(turns());
        turnList.addLast(turn);

        return new Game(id(), turnList, createdAt(), maxStepsReached(turnList));
    }
}
