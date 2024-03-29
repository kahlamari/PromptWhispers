package in.kahl.promptwhispers.controller;

import in.kahl.promptwhispers.model.ErrorMessage;
import in.kahl.promptwhispers.model.Lobby;
import in.kahl.promptwhispers.service.LobbyService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/lobbies")
public class LobbyController {
    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Lobby createLobby(@AuthenticationPrincipal OAuth2User principal) {
        return lobbyService.createLobby(principal);
    }

    @GetMapping("{id}")
    public Lobby getLobbyById(@PathVariable String id) {
        return lobbyService.getLobbyById(id);
    }

    @PutMapping("{id}/join")
    public Lobby joinLobby(@AuthenticationPrincipal OAuth2User principal, @PathVariable String id) {
        return lobbyService.joinLobby(principal, id);
    }

    @PutMapping("{id}/leave")
    public Lobby leaveLobby(@AuthenticationPrincipal OAuth2User principal, @PathVariable String id) {
        return lobbyService.leaveLobby(principal, id);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLobby(@AuthenticationPrincipal OAuth2User principal, @PathVariable String id) {
        lobbyService.deleteLobby(principal, id);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleLobbyNotFound() {
        return new ErrorMessage("NoSuchElementException: The lobby of your request does not exist.");
    }
}
