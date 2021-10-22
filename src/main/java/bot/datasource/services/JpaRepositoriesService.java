package bot.datasource.services;

import bot.datasource.repositories.PostsRepository;
import bot.datasource.repositories.StatisticsRepository;
import bot.datasource.repositories.UsersRepository;
import bot.entities.BotUser;
import bot.entities.Post;
import bot.entities.Statistic;
import bot.utils.Formatter;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JpaRepositoriesService implements DBService {

    private final UsersRepository usersRepository;
    private final PostsRepository postsRepository;
    private final StatisticsRepository statisticsRepository;

    public JpaRepositoriesService(UsersRepository usersRepository, PostsRepository postsRepository,
                                  StatisticsRepository statisticsRepository) {
        this.usersRepository = usersRepository;
        this.postsRepository = postsRepository;
        this.statisticsRepository = statisticsRepository;
    }

    // users

    @Override
    public BotUser getUser(Long chatId) {
        BotUser botUser = usersRepository.findById(chatId).orElse(null);

        if (botUser == null) {
            botUser = new BotUser(chatId);
            saveUser(botUser);
        }

        return botUser;
    }

    @Override
    public void saveUser(BotUser user) {
        usersRepository.save(user);
    }

    // posts

    @Override
    public Post getPost(Integer id) {
        return postsRepository.findById(id).orElse(null);
    }

    @Override
    public void savePost(Post post) {
        postsRepository.save(post);
    }

    @Override
    public void deletePost(Post post) {
        postsRepository.delete(post);
    }

    // stats

    @Override
    public int countPostedPosts(BotUser user) {
        return postsRepository.countAllByCreatorAndPosted(user.getChatId());
    }

    @Override
    public int getPostedPostsTop(BotUser user) {
        List<BotUser> top = usersRepository.findAll().stream()
                .sorted((topUser1, topUser2) -> {
                    int first = countPostedPosts(topUser1), second = countPostedPosts(topUser2);

                    return first == second ? getLikesSum(topUser2) - getLikesSum(topUser1) : second - first;
                })
                .toList();

        return top.indexOf(user) + 1;
    }

    @Override
    public int getLikesSum(BotUser user) {
        return postsRepository.findAllById(user.getCreatedPostsIds()).stream()
                .mapToInt(Post::getLikesCount)
                .sum();
    }

    @Override
    public int getLikesTop(BotUser user) {
        List<BotUser> top = usersRepository.findAll().stream()
                .sorted((topUser1, topUser2) -> {
                    int first = getLikesSum(topUser1), second = getLikesSum(topUser2);

                    return first == second ? countPostedPosts(topUser1) - countPostedPosts(topUser2) : second - first;
                })
                .toList();

        return top.indexOf(user) + 1;
    }

    @Override
    public float getLikesPerPost(BotUser user) {
        List<Post> posts = postsRepository.findAllById(user.getCreatedPostsIds());

        int postsCount = posts.size();
        int likes = posts.stream().mapToInt(Post::getLikesCount).sum();

        return postsCount == 0 ? 0 : Formatter.round((float) likes / postsCount, 2);
    }

    @Override
    public int getLikesPerPostTop(BotUser user) {
        List<BotUser> top = usersRepository.findAll().stream()
                .filter(topUser -> topUser.getCreatedPostsIds().size() >= 5)
                .sorted(Comparator.comparing(topUser -> -getLikesPerPost(topUser)))
                .toList();

        return top.indexOf(user) + 1;
    }

    private List<Post> get10LastPosts(BotUser user) {
        return postsRepository.findAllById(user.getCreatedPostsIds()).stream()
                .sorted(Comparator.comparing(post -> -post.getId()))
                .limit(10)
                .toList();
    }

    @Override
    public int get10LastPostsLikesSum(BotUser user) {
        return get10LastPosts(user).stream().mapToInt(Post::getLikesCount).sum();
    }

    @Override
    public int get10LastPostsLikesTop(BotUser user) {
        List<BotUser> top = usersRepository.findAll().stream()
                .sorted(Comparator.comparing(topUser -> -get10LastPostsLikesSum(topUser)))
                .toList();

        return top.indexOf(user) + 1;
    }

    @Override
    public float get10LastPostsLikesPerPost(BotUser user) {
        List<Post> posts = get10LastPosts(user);

        int postsCount = posts.size();
        int likes = posts.stream().mapToInt(Post::getLikesCount).sum();

        return postsCount == 0 ? 0 : Formatter.round((float) likes / postsCount, 2);
    }

    @Override
    public int get10LastPostsLikesPerPostTop(BotUser user) {
        List<BotUser> top = usersRepository.findAll().stream()
                .filter(topUser -> topUser.getCreatedPostsIds().size() >= 5)
                .sorted(Comparator.comparing(topUser -> -get10LastPostsLikesPerPost(topUser)))
                .toList();

        return top.indexOf(user) + 1;
    }

    // daily stats

    @Override
    public Statistic getTodayStatistics() {
        return statisticsRepository.getToday();
    }

    @Override
    public Statistic getYesterdayStatistics() {
        return statisticsRepository.getByDate(new Date(new Date().getTime() - 24 * 60 * 60 * 1000));
    }

    @Override
    public void saveStatistics(Statistic statistic) {
        statisticsRepository.save(statistic);
    }
}
