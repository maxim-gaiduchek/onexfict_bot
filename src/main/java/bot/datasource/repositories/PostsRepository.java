package bot.datasource.repositories;

import bot.entities.BotUser;
import bot.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostsRepository extends JpaRepository<Post, Integer> {

    @Query("SELECT COUNT(post) FROM Post post WHERE post.isPosted = TRUE  AND post.creator = ?1")
    int countAllByCreatorAndPosted(BotUser user);
}
