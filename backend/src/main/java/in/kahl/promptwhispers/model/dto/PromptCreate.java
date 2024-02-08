package in.kahl.promptwhispers.model.dto;

import in.kahl.promptwhispers.model.Step;
import in.kahl.promptwhispers.model.StepType;

public record PromptCreate(
        String prompt
) {
    public Step makeIntoPrompt() {
        return new Step(StepType.PROMPT, prompt());
    }
}
