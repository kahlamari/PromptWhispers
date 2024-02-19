package in.kahl.promptwhispers.model.dto;

import in.kahl.promptwhispers.model.Turn;

import java.time.Instant;
import java.util.List;

public record GameResponse(
        String id,
        List<Turn> turns,
        Instant createdAt,
        boolean isFinished
) {
}
