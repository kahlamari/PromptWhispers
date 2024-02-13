package in.kahl.promptwhispers.controller;

import in.kahl.promptwhispers.model.ErrorMessage;
import in.kahl.promptwhispers.model.Game;
import in.kahl.promptwhispers.model.dto.PromptCreate;
import in.kahl.promptwhispers.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
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

    @PostMapping("start")
    @ResponseStatus(HttpStatus.CREATED)
    public Game createGame(@AuthenticationPrincipal OAuth2User principal) {
        return gameService.createGame(principal);
    }

    @GetMapping("{gameId}")
    public Game getGame(@PathVariable String gameId) {
        return gameService.getGameById(gameId);
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
    public Game submitPrompt(@PathVariable String gameId, @RequestBody PromptCreate prompt) {
        return gameService.submitPrompt(gameId, prompt);
    }

    @PostMapping("{gameId}/generateImage")
    @ResponseStatus(HttpStatus.CREATED)
    public Game generateImage(@PathVariable String gameId) {
        return gameService.generateImage(gameId);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleGameNotFound() {
        return new ErrorMessage("NoSuchElementException. The game associated with your request does not exist.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessage handleAccessDenied() {
        return new ErrorMessage("AccessDeniedException. Your request tries to access a game you do not have privileges to access.");
    }
}
