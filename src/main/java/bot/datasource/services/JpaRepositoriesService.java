package bot.datasource.services;

import bot.datasource.repositories.PostsRepository;
import bot.datasource.repositories.UsersRepository;
import bot.entities.BotUser;
import bot.entities.Post;
import org.springframework.stereotype.Service;

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
