package in.kahl.promptwhispers.model.dto;

import in.kahl.promptwhispers.model.Prompt;

public record PromptCreate(
        String prompt
) {
    public Prompt makeIntoPrompt() {
        return new Prompt(prompt());
    }
}
