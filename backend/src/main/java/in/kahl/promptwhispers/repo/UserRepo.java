package in.kahl.promptwhispers.repo;

import in.kahl.promptwhispers.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepo extends MongoRepository<User, String> {
    Boolean existsByEmail(String email);

    User getUserByEmail(String email);
}
