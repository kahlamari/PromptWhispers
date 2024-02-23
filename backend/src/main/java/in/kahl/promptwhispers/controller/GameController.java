package in.kahl.promptwhispers.controller;

import in.kahl.promptwhispers.model.ErrorMessage;
import in.kahl.promptwhispers.model.Game;
import in.kahl.promptwhispers.model.Lobby;
import in.kahl.promptwhispers.model.dto.PromptCreate;
import in.kahl.promptwhispers.model.dto.RoundResponse;
import in.kahl.promptwhispers.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoundResponse createGame(@AuthenticationPrincipal OAuth2User principal, @RequestBody Lobby lobby) {
        return gameService.createGame(principal, lobby);
    }

    @GetMapping("{gameId}")
    public RoundResponse getGame(@AuthenticationPrincipal OAuth2User principal, @PathVariable String gameId) {
        return gameService.getGameById(principal, gameId);
    }

    @GetMapping("{gameId}/all")
    public Game getGameAll(@PathVariable String gameId) {
        return gameService.getGameAllById(gameId);
    }

    @GetMapping()
    public List<Game> getAllGames(@AuthenticationPrincipal OAuth2User principal) {
        return gameService.getGamesByUser(principal);
    }

    @DeleteMapping("{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@AuthenticationPrincipal OAuth2User principal, @PathVariable String gameId) {
        gameService.deleteGame(principal, gameId);
    }

    @PostMapping("{gameId}/prompt")
    @ResponseStatus(HttpStatus.CREATED)
    public RoundResponse submitPrompt(@AuthenticationPrincipal OAuth2User principal, @PathVariable String gameId, @RequestBody PromptCreate prompt) {
        return gameService.submitPrompt(principal, gameId, prompt);
    }

    @PostMapping("{gameId}/generateImage")
    @ResponseStatus(HttpStatus.CREATED)
    public RoundResponse generateImage(@AuthenticationPrincipal OAuth2User principal, @PathVariable String gameId) {
        return gameService.generateImage(principal, gameId);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleGameNotFound() {
        return new ErrorMessage("NoSuchElementException: The game associated with your request does not exist.");
    }
}
