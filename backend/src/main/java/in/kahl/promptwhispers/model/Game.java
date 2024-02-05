package in.kahl.promptwhispers.model;

import in.kahl.promptwhispers.Renderable;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record Game(
        @Id
        String id,
        Map<Integer, Renderable> steps,
        Instant createdAt,
        Boolean isFinished
) {
    public Game() {
        this(UUID.randomUUID().toString(), Collections.emptyMap(), Instant.now(), false);
    }

    public Game withStep(Renderable step) {
        HashMap<Integer, Renderable> map = new HashMap<>(steps());

        int nextOrder = map.size();
        map.put(nextOrder, step);
        return new Game(id(), map, createdAt(), isFinished());
    }
}
