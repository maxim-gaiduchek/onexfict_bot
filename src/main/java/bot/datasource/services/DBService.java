package bot.datasource.services;

import bot.entities.Post;
import bot.entities.BotUser;
import bot.entities.Statistic;

import java.util.List;

public interface DBService {

    // users

    BotUser getUser(Long id);

    void saveUser(BotUser user);

    int countPostedPosts(BotUser user);

    int countAllPostedPosts();

    int countAllTodayPostedPosts();

    int getPostedPostsTop(BotUser user);

    int getLikesSum(BotUser user);

    int getAllLikesSum();

    int getLikesTop(BotUser user);

    int getLikesPerPostTop(BotUser user);

    // posts

    Post getPost(Integer id);

    void savePost(Post post);

    void deletePost(Post post);

    // daily stats

    Statistic getTodayStatistics();

    Statistic getYesterdayStatistics();

    void saveStatistics(Statistic statistic);
}
