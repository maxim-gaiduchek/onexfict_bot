package bot.datasource.services;

import bot.entities.Post;
import bot.entities.BotUser;

import java.util.List;

public interface DBService {

    // users

    BotUser getUser(Long id);

    void saveUser(BotUser user);

    int getPostedPostsCount(BotUser user);

    int getLikesSum(List<Integer> ids);

    // posts

    Post getPost(Integer id);

    void savePost(Post post);

    void deletePost(Post post);
}
