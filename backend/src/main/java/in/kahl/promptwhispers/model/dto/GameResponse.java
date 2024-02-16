package in.kahl.promptwhispers.model.dto;

import in.kahl.promptwhispers.model.Step;

import java.time.Instant;
import java.util.List;

public record GameResponse(
        String id,
        List<Step> steps,
        Instant createdAt,
        boolean isFinished
) {
}
