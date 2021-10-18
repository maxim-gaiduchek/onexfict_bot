package bot.datasource.services;

import bot.datasource.repositories.PostsRepository;
import bot.datasource.repositories.UsersRepository;
import bot.entities.BotUser;
import bot.entities.Post;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JpaRepositoriesService implements DBService {

    private final UsersRepository usersRepository;
    private final PostsRepository postsRepository;

    public JpaRepositoriesService(UsersRepository usersRepository, PostsRepository postsRepository) {
        this.usersRepository = usersRepository;
        this.postsRepository = postsRepository;
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
    public int getPostedPostsCount(BotUser user) {
        return (int) postsRepository.findAllById(user.getCreatedPostsIds()).stream()
                .filter(post -> !post.isNotPosted())
                .count();
    }

    @Override
    public int getPostedPostsTop(BotUser user) {
        List<BotUser> top = usersRepository.findAll().stream()
                .sorted(Comparator.comparing(topUser -> -getPostedPostsCount(topUser)))
                .toList();

        return top.indexOf(user);
    }

    @Override
    public int getLikesSum(List<Integer> ids) {
        return postsRepository.findAllById(ids).stream()
                .mapToInt(Post::getLikesCount)
                .sum();
    }

    @Override
    public int getLikesTop(BotUser user) {
        List<BotUser> top = usersRepository.findAll().stream()
                .sorted(Comparator.comparing(topUser -> -getLikesSum(topUser.getCreatedPostsIds())))
                .toList();

        return top.indexOf(user);
    }

    @Override
    public int getLikesPerPostTop(BotUser user) {
        Map<BotUser, Float> top = new HashMap<>();

        for (BotUser topUser : usersRepository.findAll()) {
            int posts = topUser.getCreatedPostsIds().size();
            int likes = getLikesSum(topUser.getCreatedPostsIds());
            float likesPerPosts = posts == 0 ? 0 : -(float) (((int) Math.round(100.0 * likes / posts)) / 100.0);

            top.put(topUser, likesPerPosts);
        }

        List<BotUser> topUsers = top.entrySet().stream()
                .sorted(Comparator.comparing(entry -> -entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        return topUsers.indexOf(user);
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
}
