package in.kahl.promptwhispers.model.dto;

import in.kahl.promptwhispers.model.Turn;
import in.kahl.promptwhispers.model.TurnType;

public record PromptCreate(
        String prompt
) {
    public Turn makeIntoPrompt() {
        return new Turn(TurnType.PROMPT, prompt());
    }
}
