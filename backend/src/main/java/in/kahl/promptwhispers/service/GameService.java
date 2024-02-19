package in.kahl.promptwhispers.service;

import in.kahl.promptwhispers.model.*;
import in.kahl.promptwhispers.model.dto.GameResponse;
import in.kahl.promptwhispers.model.dto.PromptCreate;
import in.kahl.promptwhispers.repo.GameRepo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class GameService {
    private final GameRepo gameRepo;

    private final UserService userService;

    private final LobbyService lobbyService;

    private final DalleService dalleService;

    private final CloudinaryService cloudinaryService;

    public GameService(GameRepo gameRepo, UserService userService, LobbyService lobbyService, DalleService dalleService, CloudinaryService cloudinaryService) {
        this.gameRepo = gameRepo;
        this.userService = userService;
        this.lobbyService = lobbyService;
        this.dalleService = dalleService;
        this.cloudinaryService = cloudinaryService;
    }

    public GameResponse createGame(OAuth2User principal) {
        User user = userService.getLoggedInUser(principal);
        Game newGame = gameRepo.save(new Game(user));
        userService.save(user.withGame(newGame));

        return newGame.asGameResponse();
    }

    public GameResponse createGame(OAuth2User principal, Lobby lobby) {
        User user = userService.getLoggedInUser(principal);
        Game newGame = gameRepo.save(new Game(user));
        userService.save(user.withGame(newGame));
        lobbyService.update(lobby.withGameId(newGame.id()));

        return newGame.asGameResponse();
    }

    public GameResponse getGameById(String id) {
        return gameRepo.findById(id).orElseThrow(NoSuchElementException::new).asGameResponse();
    }

    public List<Game> getGamesByUser(OAuth2User principal) {
        User user = userService.getLoggedInUser(principal);
        List<String> gameIds = userService.getAllGameIds(user.id());

        if (gameIds == null || gameIds.isEmpty()) {
            return Collections.emptyList();
        }

        return gameRepo.findAllById(gameIds).stream().sorted(Comparator.comparing(Game::createdAt)).toList();
    }

    public void deleteGame(OAuth2User principal, String gameId) {
        User user = userService.getLoggedInUser(principal);
        Game game = gameRepo.findById(gameId).orElseThrow(NoSuchElementException::new);

        if (user.gameIds().contains(game.id())) {
            userService.removeGame(user, game);
            gameRepo.delete(game);
        } else {
            throw new AccessDeniedException("You are not allowed to delete this game.");
        }
    }

    private static Turn getMostRecentPrompt(List<Turn> turns) {
        if (turns.isEmpty()) {
            throw new NoSuchElementException();
        }

        Turn turn = turns.getLast();

        if (turn.type().equals(TurnType.PROMPT)) {
            return turn;
        }
        throw new NoSuchElementException();
    }

    public Game submitPrompt(String gameId, PromptCreate promptCreate) {
        Turn newPrompt = promptCreate.makeIntoPrompt();

        Game game = gameRepo.findById(gameId).orElseThrow(NoSuchElementException::new);
        Game gameWithPrompt = game.withTurn(newPrompt);

        return gameRepo.save(gameWithPrompt);
    }

    public Game generateImage(String gameId) {
        Game game = gameRepo.findById(gameId).orElseThrow(NoSuchElementException::new);

        Turn prompt = getMostRecentPrompt(game.turns());

        String imageUrlDalle = dalleService.getGeneratedImageUrl(prompt.content());
        String imageUrl = cloudinaryService.uploadImage(imageUrlDalle);
        Turn generatedImage = new Turn(TurnType.IMAGE, imageUrl);
        Game gameWithImage = game.withTurn(generatedImage);

        return gameRepo.save(gameWithImage);
    }
}
