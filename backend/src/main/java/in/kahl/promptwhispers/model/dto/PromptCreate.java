package in.kahl.promptwhispers.model.dto;

import in.kahl.promptwhispers.model.Turn;
import in.kahl.promptwhispers.model.TurnType;
import in.kahl.promptwhispers.model.User;

public record PromptCreate(
        String prompt
) {
    public Turn asNewPromptTurn(User player) {
        return new Turn(player, TurnType.PROMPT, prompt());
    }
}
