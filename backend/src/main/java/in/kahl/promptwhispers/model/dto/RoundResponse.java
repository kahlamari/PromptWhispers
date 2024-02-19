package in.kahl.promptwhispers.model.dto;

import in.kahl.promptwhispers.model.Turn;

import java.util.List;

public record RoundResponse(
        String gameId,
        List<Turn> turns,
        boolean isGameFinished
) {
}
