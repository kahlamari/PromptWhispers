package in.kahl.promptwhispers.model;

import in.kahl.promptwhispers.Renderable;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public record Game(
        @Id
        String id,
        List<Renderable> steps,
        Instant createdAt,
        Boolean isFinished
) {
    public Game() {
        this(UUID.randomUUID().toString(), Collections.emptyList(), Instant.now(), false);
    }

    public Game withStep(Renderable step) {
        List<Renderable> stepList = new LinkedList<>(steps());
        stepList.addLast(step);

        return new Game(id(), stepList, createdAt(), isFinished());
    }
}
