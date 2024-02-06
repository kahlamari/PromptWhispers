package in.kahl.promptwhispers.repo;

import in.kahl.promptwhispers.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameRepo extends MongoRepository<Game, String> {

}
