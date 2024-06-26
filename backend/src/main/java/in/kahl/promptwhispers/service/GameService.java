package in.kahl.promptwhispers.service;

import in.kahl.promptwhispers.model.*;
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

    public Game createGame(OAuth2User principal, Lobby lobby) {
        User user = userService.getLoggedInUser(principal);
        User host = userService.getUserById(lobby.host().id());
        if (!user.equals(host)) {
            throw new IllegalStateException("Only the host can start a new game!");
        }

        Game newGame = new Game().withPlayer(host);

        for (User playerInLobby : lobby.players()) {
            User player = userService.getUserById(playerInLobby.id());
            newGame = newGame.withPlayer(player);
            userService.save(player.withGameId(newGame.id()));
        }

        newGame = gameRepo.save(newGame.withGameState(GameState.REQUEST_NEW_PROMPTS));
        lobbyService.update(lobby.withGameId(newGame.id()));

        return newGame;
    }

    public Game getGameById(String id) {
        return gameRepo.findById(id).orElseThrow(NoSuchElementException::new);
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

    public Game submitPrompt(OAuth2User principal, String gameId, PromptCreate promptCreate) {
        User user = userService.getLoggedInUser(principal);
        Game game = gameRepo.findById(gameId).orElseThrow(NoSuchElementException::new);

        Turn newPrompt = promptCreate.asNewPromptTurn(user);
        Game gameWithPrompt = game.withTurn(newPrompt);

        return gameRepo.save(gameWithPrompt);
    }

    public Game generateImage(OAuth2User principal, String gameId) {
        User user = userService.getLoggedInUser(principal);
        Game game = gameRepo.findById(gameId).orElseThrow(NoSuchElementException::new);

        Turn prompt = game.getMostRecentPromptByPlayer(user);

        String imageUrlDalle = dalleService.getGeneratedImageUrl(prompt.content());
        String imageUrl = cloudinaryService.uploadImage(imageUrlDalle);

        // Pull game again to ensure that concurrent image generations are pulled.
        game = gameRepo.findById(gameId).orElseThrow(NoSuchElementException::new);
        Turn generatedImage = new Turn(user, TurnType.IMAGE, imageUrl);
        Game gameWithImage = game.withTurn(generatedImage);

        return gameRepo.save(gameWithImage);
    }
}
