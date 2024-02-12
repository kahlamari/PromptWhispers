package in.kahl.promptwhispers.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public record Game(
        @Id
        String id,
        @DBRef
        User user,
        List<Step> steps,
        Instant createdAt,
        boolean isFinished
) {
    public Game(User user) {
        this(UUID.randomUUID().toString(), user, Collections.emptyList(), Instant.now(), false);
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

        return new Game(id(), user(), stepList, createdAt(), maxStepsReached(stepList));
    }
}
