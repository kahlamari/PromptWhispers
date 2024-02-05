package in.kahl.promptwhispers.service;

import in.kahl.promptwhispers.model.Game;
import in.kahl.promptwhispers.model.Prompt;
import in.kahl.promptwhispers.model.PromptCreate;
import in.kahl.promptwhispers.repo.GameRepo;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class GameService {
    private final GameRepo gameRepo;

    public GameService(GameRepo gameRepo) {
        this.gameRepo = gameRepo;
    }

    public Game createGame() {
        return gameRepo.save(new Game());
    }

    public Game getGameById(String id) {
        return gameRepo.findById(id).orElseThrow(NoSuchElementException::new);
    }

    public Game submitPrompt(String gameId, PromptCreate prompt) {
        Prompt newPrompt = prompt.withIdAndCreatedAt();

        Game game = getGameById(gameId);
        Game gameWithPrompt = game.withStep(newPrompt);

        return gameRepo.save(gameWithPrompt);
    }
}
