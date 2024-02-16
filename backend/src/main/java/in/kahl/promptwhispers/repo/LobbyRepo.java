package in.kahl.promptwhispers.repo;

import in.kahl.promptwhispers.model.Lobby;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LobbyRepo extends MongoRepository<Lobby, String> {
}
