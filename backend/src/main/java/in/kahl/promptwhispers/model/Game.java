package in.kahl.promptwhispers.model;

import in.kahl.promptwhispers.model.dto.GameResponse;
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
        Map<Integer, List<Step>> rounds,
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

    public boolean stepCompleted() {
        int minStepNumber = getLastCompletedStep();
        return rounds().values().stream().allMatch(steps -> steps.size() == minStepNumber);
    }

    public Game withStep(Step step) {
        int playerIndex = players().indexOf(step.player());

        // first step
        int offset = playerIndex + getLastCompletedStep();

        ArrayList<Step> updatedSteps = new ArrayList<>(rounds().get(offset));
        updatedSteps.add(step);

        rounds().put(offset, updatedSteps);

        return new Game(id(),
                players(),
                rounds(),
                gameState(),
                createdAt());
    }

    /*
    public static final int MAX_IMAGE_STEPS = 3;

    public static boolean maxStepsReached(List<Step> steps) {
        return steps.stream().filter(step -> step.type().equals(StepType.IMAGE)).count() >= MAX_IMAGE_STEPS;
    }

    public Game withStep(Step step) {
        if (isFinished()) {
            throw new IllegalArgumentException("Game is finished. Not steps can be added.");
        }
        List<Step> stepList = new LinkedList<>(steps());
        stepList.addLast(step);

        return new Game(id(), stepList, createdAt(), maxStepsReached(stepList));
    }
    */

}
