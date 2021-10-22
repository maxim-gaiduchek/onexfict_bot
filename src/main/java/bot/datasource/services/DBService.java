package bot.datasource.services;

import bot.entities.Post;
import bot.entities.BotUser;
import bot.entities.Statistic;

import java.util.List;

public interface DBService {

    // users

    BotUser getUser(Long id);

    void saveUser(BotUser user);

    // posts

    Post getPost(Integer id);

    void savePost(Post post);

    void deletePost(Post post);

    // stats

    int countPostedPosts(BotUser user);

    int getPostedPostsTop(BotUser user);

    int getLikesSum(BotUser user);

    int getLikesTop(BotUser user);

    float getLikesPerPost(BotUser user);

    int getLikesPerPostTop(BotUser user);

    int get10LastPostsLikesSum(BotUser user);

    int get10LastPostsLikesTop(BotUser user);

    float get10LastPostsLikesPerPost(BotUser user);

    int get10LastPostsLikesPerPostTop(BotUser user);

    // daily stats

    Statistic getTodayStatistics();

    Statistic getYesterdayStatistics();

    void saveStatistics(Statistic statistic);
}
