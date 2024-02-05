package in.kahl.promptwhispers.model;

public record PromptCreate(
        String prompt
) {
    public Prompt withIdAndCreatedAt() {
        return new Prompt(prompt());
    }
}
