package bot.datasource.repositories;

import bot.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostsRepository extends JpaRepository<Post, Integer> {
}
