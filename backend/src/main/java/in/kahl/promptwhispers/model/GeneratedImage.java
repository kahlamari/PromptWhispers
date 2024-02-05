package in.kahl.promptwhispers.model;

import in.kahl.promptwhispers.Renderable;
import org.springframework.data.annotation.Id;

public record GeneratedImage(
        @Id
        String id,
        String imageUrl,
        String createdAt
) implements Renderable {
    @Override
    public String render() {
        return imageUrl();
    }
}
