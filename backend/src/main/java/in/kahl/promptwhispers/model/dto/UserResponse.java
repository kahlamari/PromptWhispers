package in.kahl.promptwhispers.model.dto;

import in.kahl.promptwhispers.model.User;

public record UserResponse(
        String id,
        String email
) {
    public UserResponse(User user) {
        this(user.id(), user.email());
    }
}
