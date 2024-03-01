package in.kahl.promptwhispers.model.openai;

public record DalleRequest(
        String model,
        String prompt,
        int n,
        String size
) {
    public DalleRequest(String model, String prompt) {
        this(model, prompt, 1, "1024x1024");
    }

    public DalleRequest(String prompt) {
        this("dall-e-3", prompt);
    }
}
