package bot.datasource.repositories;

import bot.entities.BotUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<BotUser, Long> {
}
