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
        List<Step> steps,
        Instant createdAt,
        boolean isFinished
) {
    public Game() {
        this(UUID.randomUUID().toString(), Collections.emptyList(), Instant.now().truncatedTo(ChronoUnit.MILLIS), false);
    }

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
}
