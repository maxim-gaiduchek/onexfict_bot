package bot.datasource.services;

import bot.entities.Post;
import bot.entities.BotUser;

public interface DBService {

    // users

    BotUser getUser(Long id);

    void saveUser(BotUser user);

    // posts

    Post getPost(Integer id);

    void savePost(Post post);

    void deletePost(Post post);
}
