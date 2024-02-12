package in.kahl.promptwhispers.service;

import in.kahl.promptwhispers.model.User;
import in.kahl.promptwhispers.model.dto.UserResponse;
import in.kahl.promptwhispers.repo.UserRepo;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public User getLoggedInUser(OAuth2User user) {
        if (user == null) {
            return null;
        }

        String userEmail = user.getAttribute("email");

        if (userEmail == null || userEmail.isEmpty()) {
            return null;
        }

        userEmail = userEmail.trim();

        return userRepo.getUserByEmail(userEmail);
    }

    public UserResponse getLoggedInUserAsUserResponse(OAuth2User user) {
        User loggedInUser = getLoggedInUser(user);
        if (loggedInUser == null) {
            return null;
        }
        return new UserResponse(loggedInUser);
    }
}
