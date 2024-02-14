package in.kahl.promptwhispers.service;

import in.kahl.promptwhispers.model.Lobby;
import in.kahl.promptwhispers.model.User;
import in.kahl.promptwhispers.repo.LobbyRepo;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class LobbyService {
    private final LobbyRepo lobbyRepo;
    private final UserService userService;

    public LobbyService(LobbyRepo lobbyRepo, UserService userService) {
        this.lobbyRepo = lobbyRepo;
        this.userService = userService;
    }

    public Lobby createLobby(OAuth2User principal) {
        User user = userService.getLoggedInUser(principal);
        return lobbyRepo.save((new Lobby(user)));
    }

    public Lobby getLobbyById(String id) {
        return lobbyRepo.findById(id).orElseThrow(NoSuchElementException::new);
    }
}
