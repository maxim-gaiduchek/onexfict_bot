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

    @Override
    public int countPostedPosts(BotUser user) {
        return postsRepository.countAllByCreatorAndPosted(user);
    }

    @Override
    public int countAllPostedPosts() {
        return postsRepository.countAllByPosted();
    }

    @Override
    public int countAllTodayPostedPosts() {
        return postsRepository.countAllTodayPostedPosts();
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
    public int getAllLikesSum() {
        return postsRepository.findAll().stream()
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
    public int getLikesPerPostTop(BotUser user) {
        Map<BotUser, Float> top = new HashMap<>();

        for (BotUser topUser : usersRepository.findAll()) {
            List<Post> posts = postsRepository.findAllById(topUser.getCreatedPostsIds()).stream()
                    .sorted(Comparator.comparing(post -> -post.getId()))
                    .limit(10)
                    .toList();

            int postsCount = posts.size();
            int likes = posts.stream().mapToInt(Post::getLikesCount).sum();

            float likesPerPosts = postsCount == 0 ? 0 : Formatter.round((float) likes / postsCount, 2);

            top.put(topUser, likesPerPosts);
        }

        List<BotUser> topUsers = top.entrySet().stream()
                .filter(entry -> entry.getKey().getCreatedPostsIds().size() >= 5)
                .sorted(Comparator.comparing(entry -> -entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        return topUsers.indexOf(user) + 1;
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

    // daily stats

    @Override
    public Statistic getTodayStatistics() {
        return statisticsRepository.getToday();
    }

    @Override
    public Statistic getYesterdayStatistics() {
        Statistic today = statisticsRepository.getToday();

        return statisticsRepository.getByDate(new Date(today.getDate().getTime() - 24 * 60 * 60 * 1000));
    }

    @Override
    public void saveStatistics(Statistic statistic) {
        statisticsRepository.save(statistic);
    }
}
