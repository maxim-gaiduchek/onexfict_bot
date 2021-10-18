package bot.datasource.services;

import bot.datasource.repositories.PostsRepository;
import bot.datasource.repositories.UsersRepository;
import bot.entities.BotUser;
import bot.entities.Post;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public int getLikesSum(List<Integer> ids) {
        return postsRepository.findAllById(ids).stream()
                .mapToInt(Post::getLikesCount)
                .sum();
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
