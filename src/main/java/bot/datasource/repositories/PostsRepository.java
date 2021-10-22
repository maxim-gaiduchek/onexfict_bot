package bot.datasource.repositories;

import bot.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostsRepository extends JpaRepository<Post, Integer> {

    @Query("SELECT COUNT(post) FROM Post post WHERE post.isPosted = TRUE  AND post.creatorChatId = ?1")
    int countAllByCreatorAndPosted(Long creatorChatId);
}
