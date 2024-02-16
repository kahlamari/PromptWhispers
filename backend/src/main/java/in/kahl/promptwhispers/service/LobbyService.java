package in.kahl.promptwhispers.service;

import in.kahl.promptwhispers.model.Lobby;
import in.kahl.promptwhispers.model.User;
import in.kahl.promptwhispers.repo.LobbyRepo;
import org.springframework.security.access.AccessDeniedException;
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

    public Lobby joinLobby(OAuth2User principal, String id) {
        User user = userService.getLoggedInUser(principal);
        Lobby lobby = getLobbyById(id);

        if (lobby.host().equals(user)) {
            return lobby;
        }

        lobby.players().add(user);
        return lobbyRepo.save(lobby);
    }

    public Lobby leaveLobby(OAuth2User principal, String id) {
        User user = userService.getLoggedInUser(principal);
        Lobby lobby = getLobbyById(id);

        if (lobby.host().equals(user)) {
            throw new AccessDeniedException("You cannot leave the lobby when you are the host.");
        }

        lobby.players().remove(user);
        return lobbyRepo.save(lobby);
    }

    public void deleteLobby(OAuth2User principal, String id) {
        User user = userService.getLoggedInUser(principal);
        Lobby lobby = getLobbyById(id);

        if (lobby.host().equals(user)) {
            lobbyRepo.delete(lobby);
        } else {
            throw new AccessDeniedException("You are not allowed to delete this lobby.");
        }
    }

    public void update(Lobby lobby) {
        lobbyRepo.save(lobby);
    }
}
