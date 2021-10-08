package datasource.repositories;

import entities.BotUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface UsersRepository extends JpaRepository<BotUser, Long> {
}
