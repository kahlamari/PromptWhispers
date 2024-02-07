package in.kahl.promptwhispers.controller;

import in.kahl.promptwhispers.model.Game;
import in.kahl.promptwhispers.model.dto.PromptCreate;
import in.kahl.promptwhispers.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("start")
    @ResponseStatus(HttpStatus.CREATED)
    public Game createGame() {
        return gameService.createGame();
    }

    @GetMapping("{gameId}")
    public Game getGame(@PathVariable String gameId) {
        return gameService.getGameById(gameId);
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
}
