package in.kahl.promptwhispers.service;

import in.kahl.promptwhispers.Renderable;
import in.kahl.promptwhispers.model.Game;
import in.kahl.promptwhispers.model.GeneratedImage;
import in.kahl.promptwhispers.model.Prompt;
import in.kahl.promptwhispers.model.dto.PromptCreate;
import in.kahl.promptwhispers.repo.GameRepo;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class GameService {
    private final GameRepo gameRepo;

    private final DalleService dalleService;

    private final CloudinaryService cloudinaryService;

    public GameService(GameRepo gameRepo, DalleService dalleService, CloudinaryService cloudinaryService) {
        this.gameRepo = gameRepo;
        this.dalleService = dalleService;
        this.cloudinaryService = cloudinaryService;
    }

    public Game createGame() {
        return gameRepo.save(new Game());
    }

    public Game getGameById(String id) {
        return gameRepo.findById(id).orElseThrow(NoSuchElementException::new);
    }

    public Game submitPrompt(String gameId, PromptCreate promptCreate) {
        Prompt newPrompt = promptCreate.makeIntoPrompt();

        Game game = getGameById(gameId);
        Game gameWithPrompt = game.withStep(newPrompt);

        return gameRepo.save(gameWithPrompt);
    }

    private static Prompt getMostRecentPrompt(Map<Integer, Renderable> steps) {
        if (steps.isEmpty()) {
            throw new NoSuchElementException();
        }

        Integer i = steps.size() - 1;

        Renderable step = steps.get(i);

        if (step instanceof Prompt) {
            return (Prompt) steps.get(i);
        }
        throw new NoSuchElementException();
    }

    public Game generateImage(String gameId) {
        Game game = getGameById(gameId);

        Prompt prompt = getMostRecentPrompt(game.steps());

        String imageUrlDalle = dalleService.getGeneratedImageUrl(prompt.prompt());
        String imageUrl = cloudinaryService.uploadImage(imageUrlDalle);
        GeneratedImage generatedImage = new GeneratedImage(imageUrl);
        Game gameWithImage = game.withStep(generatedImage);

        return gameRepo.save(gameWithImage);
    }
}
