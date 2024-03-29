package in.kahl.promptwhispers.service;

import in.kahl.promptwhispers.exception.GoogleEmailNotFoundException;
import in.kahl.promptwhispers.model.Game;
import in.kahl.promptwhispers.model.User;
import in.kahl.promptwhispers.model.dto.UserResponse;
import in.kahl.promptwhispers.repo.UserRepo;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public void save(User user) {
        userRepo.save(user);
    }

    public User getLoggedInUser(OAuth2User user) {
        if (user == null) {
            // OAuth2User object is null when the user is not logged in.
            return null;
        }

        String userEmail = user.getAttribute("email");

        if (userEmail == null || userEmail.isEmpty()) {
            throw new GoogleEmailNotFoundException("Email must be present to proceed.");
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

    public User getUserById(String playerId) {
        return userRepo.findById(playerId).orElseThrow(NoSuchElementException::new);
    }

    public boolean saveNewUser(OAuth2User oAuth2User) {
        String userEmail = oAuth2User.getAttribute("email");

        if (userEmail == null || userEmail.isEmpty()) {
            return false;
        }

        String profilePic = oAuth2User.getAttribute("picture");

        if (profilePic == null) {
            profilePic = "";
        }

        boolean isReturningUser = userRepo.existsByEmail(userEmail.trim());

        if (!isReturningUser) {
            User newUser = new User(userEmail.trim(), profilePic);
            userRepo.save(newUser);
        }

        return true;
    }

    public List<String> getAllGameIds(String userId) {
        return userRepo.findById(userId).orElseThrow(NoSuchElementException::new).gameIds();
    }

    public User removeGame(User user, Game game) {
        return userRepo.save(user.withoutGame(game));
    }
}
