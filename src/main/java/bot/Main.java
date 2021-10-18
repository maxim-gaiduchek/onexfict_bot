package bot;

import bot.controllers.AdminController;
import bot.controllers.ChannelController;
import bot.controllers.PostsCreator;
import bot.datasource.DatasourceConfig;
import bot.datasource.services.DBService;
import bot.entities.BotUser;
import bot.entities.Post;
import bot.utils.SimpleSender;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

public class Main extends TelegramLongPollingBot {

    private static final String BOT_USERNAME = System.getenv("BOT_USERNAME");
    private static final String BOT_TOKEN = System.getenv("BOT_TOKEN");

    private final SimpleSender sender = new SimpleSender(BOT_TOKEN);

    private static final ApplicationContext CONTEXT = new AnnotationConfigApplicationContext(DatasourceConfig.class);
    private final DBService service = (DBService) CONTEXT.getBean("service");

    private static final String STATS_STRING = "\uD83D\uDCCA Моя статистика";
    private static final String CREATE_POST_STRING = "\uD83D\uDCC3 Предложить пост";

    // start

    private Main() {
    }

    // parsing

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update);

        if (update.hasMessage()) {
            parseMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            parseCallbackQuery(update.getCallbackQuery());
        }
    }

    // message parsing

    private void parseMessage(Message message) {
        if (message.isUserMessage()) {
            if (message.isCommand()) {
                parseCommand(message);
            } else if (message.hasPhoto() || message.hasVideo()) {
                parseMedia(message);
            } else if (message.hasText()) {
                parseTextMessage(message);
            } else {
                sender.deleteMessage(message.getChatId(), message.getMessageId());
            }
        } else if (message.getNewChatMembers() != null) {
            Long chatId = message.getChatId();

            if (!chatId.toString().equals(AdminController.ADMIN_CHAT_ID)) {
                sender.leaveChat(chatId);
            }
        }
    }

    // commands

    private void parseCommand(Message message) {
        Long chatId = message.getChatId();
        BotUser user = service.getUser(chatId);
        String command = message.getText();

        switch (user.getStatus()) {
            case INACTIVE -> {
                switch (command) {
                    case "/start" -> startCommand(chatId);
                    case "/stats" -> statsCommand(chatId);
                    case "/post" -> {
                        PostsCreator.sendAddPhoto(sender, user);
                        service.savePost(user.getPost());
                    }
                    default -> helpCommand(chatId);
                }
            }
            case IS_ADDING_PHOTO -> {
                Post post = user.getPost();

                if (post == null || post.getImagesFilesIds().size() == 0) {
                    PostsCreator.sendAddPhoto(sender, chatId);
                } else {
                    PostsCreator.addPhoto(sender, chatId);
                }
            }
            case IS_ADDING_TEXT -> PostsCreator.sendAddText(sender, chatId);
            case IS_ADDING_BY -> PostsCreator.sendAddBy(sender, chatId);
            case IS_ADDING_SOURCE -> PostsCreator.sendAddSource(sender, chatId);
        }

        service.saveUser(user);
    }

    private void startCommand(Long chatId) {
        String msg = """
                Это предложка 1xФИВТ (@onexfict). Тут можно предложить мем или новость""";

        sender.sendStringAndKeyboard(chatId, msg, getCreatePostKeyboard(), true);
    }

    private void statsCommand(Long chatId) {
        BotUser user = service.getUser(chatId);

        int posts = user.getCreatedPostsIds().size();
        int likes = service.getLikesSum(user.getCreatedPostsIds());
        float likesPerPost = posts == 0 ? 0 : (float) (((int) Math.round(100.0 * likes / posts)) / 100.0);

        int topPosts = service.getPostedPostsTop(user);
        String topPostsString = switch (topPosts) {
            case 1 -> " (Топ 1\uD83E\uDD47)";
            case 2 -> " (Топ 2\uD83E\uDD48)";
            case 3 -> " (Топ 3\uD83E\uDD49)";
            default -> " (Топ " + topPosts + ")";
        };

        int topLikes = service.getLikesTop(user);
        String topLikesString = switch (topLikes) {
            case 1 -> " (Топ 1\uD83E\uDD47)";
            case 2 -> " (Топ 2\uD83E\uDD48)";
            case 3 -> " (Топ 3\uD83E\uDD49)";
            default -> " (Топ " + topLikes + ")";
        };

        String topLikesPerPostString = "";

        if (posts >= 5) {
            int topLikesPerPost = service.getLikesPerPostTop(user);

            topLikesPerPostString = switch (topLikesPerPost) {
                case 1 -> " (Топ 1\uD83E\uDD47)";
                case 2 -> " (Топ 2\uD83E\uDD48)";
                case 3 -> " (Топ 3\uD83E\uDD49)";
                default -> " (Топ " + topLikesPerPost + ")";
            };
        }

        String msg = "\uD83D\uDCCA *Твоя статистика*\n" +
                "\n" +
                "📃 Постов запостили: *" + posts + "*" + topPostsString + "\n" +
                "❤️ Лайков всего: *" + likes + "*" + topLikesString + "\n" +
                "\uD83D\uDC65 Лайков за пост в среднем: *" + likesPerPost + "*" + topLikesPerPostString;

        sender.sendStringAndKeyboard(chatId, msg, getCreatePostKeyboard(), true);
    }

    private void helpCommand(Long chatId) {
        String msg = """
                Это предложка 1xФИВТ (@onexfict).
                                
                Введи /post, чтоб предложить мем
                Введи /stats, чтоб глянуть свою статистику мемодела""";

        sender.sendStringAndKeyboard(chatId, msg, getCreatePostKeyboard(), true);
    }

    // photo media

    private void parseMedia(Message message) {
        Long chatId = message.getChatId();
        BotUser user = service.getUser(chatId);

        switch (user.getStatus()) {
            case IS_ADDING_PHOTO -> {
                String fileId = null;

                if (message.hasPhoto()) {
                    fileId = "photo:" + message.getPhoto().get(0).getFileId();
                } else if (message.hasVideo()) {
                    fileId = "video:" + message.getVideo().getFileId();
                }

                PostsCreator.addPhoto(sender, user, fileId);
                service.savePost(user.getPost());
            }
            case IS_ADDING_TEXT -> PostsCreator.sendAddText(sender, chatId);
            case IS_ADDING_BY -> PostsCreator.sendAddBy(sender, chatId);
        }

        service.saveUser(user);
    }

    // text parsing

    private void parseTextMessage(Message message) {
        Long chatId = message.getChatId();
        BotUser user = service.getUser(chatId);
        String text = message.getText();

        if (text.equals(PostsCreator.STOP_CREATING_POST_STRING)) {
            if (user.getStatus() != BotUser.Status.INACTIVE) {
                Post post = user.getPost();

                user.setStatus(BotUser.Status.INACTIVE);
                user.setPost(null);

                service.saveUser(user);
                service.deletePost(post);

                sender.sendStringAndKeyboard(chatId, "Создание поста прекращено", getCreatePostKeyboard(), true);
            } else {
                helpCommand(chatId);
            }
        }

        switch (user.getStatus()) {
            case INACTIVE -> {
                if (text.equals(CREATE_POST_STRING)) {
                    PostsCreator.sendAddPhoto(sender, user);
                    service.savePost(user.getPost());
                } else if (text.equals(STATS_STRING)) {
                    statsCommand(chatId);
                } else {
                    helpCommand(chatId);
                }
            }
            case IS_ADDING_PHOTO -> {
                if (text.equals(PostsCreator.STOP_ADDING_PHOTO_STRING)) {
                    PostsCreator.sendAddText(sender, user);
                } else {
                    Post post = user.getPost();

                    if (post == null || post.getImagesFilesIds().size() == 0) {
                        PostsCreator.sendAddPhoto(sender, chatId);
                    } else {
                        PostsCreator.addPhoto(sender, chatId);
                    }
                }
            }
            case IS_ADDING_TEXT -> {
                PostsCreator.addText(sender, user, text);
                service.savePost(user.getPost());
            }
            case IS_ADDING_BY -> {
                PostsCreator.addBy(sender, user, text);
                service.savePost(user.getPost());
            }
            case IS_ADDING_SOURCE -> {
                if (text.startsWith("https://")) {
                    PostsCreator.addSource(sender, user, text);

                    Post post = user.getPost();

                    AdminController.sendToAdmin(user.getPost(), message.getFrom(), sender);
                    service.savePost(post);

                    user.addCreatedPost(post.getId());
                    user.setPost(null);
                } else {
                    PostsCreator.sendSourceError(sender, user.getChatId());
                }
            }
        }

        service.saveUser(user);
    }

    // parse callback query

    private void parseCallbackQuery(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();
        Integer userId = callbackQuery.getFrom().getId();

        String text = callbackQuery.getData();
        String query = text.substring(0, text.indexOf('_'));
        String data = text.substring(text.indexOf('_') + 1);

        Post post = service.getPost(Integer.parseInt(data));

        switch (query) {
            case "admin-agree" -> {
                post.switchAgree(userId);

                AdminController.editAdminAgreeKeyboard(post, sender, messageId);
                if (post.getAgreesCount() >= AdminController.ADMIN_LIKES) {
                    ChannelController.post(post, sender);
                    sender.removeKeyboard(chatId, messageId);
                    sender.sendString(chatId, "Пост подтвержден " + post.getWhoHasAgreed() + " и запостен", messageId);
                }

                service.savePost(post);
            }
            case "post-like" -> {
                post.switchLike(userId);

                ChannelController.editPostLikesKeyboard(post, sender, messageId);
                service.savePost(post);
            }
        }
    }

    // keyboards

    public static List<KeyboardRow> getTwoRowsKeyboard(String first, String second) {
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstRow = new KeyboardRow();
        KeyboardRow secondRow = new KeyboardRow();

        firstRow.add(first);
        secondRow.add(second);

        keyboard.add(firstRow);
        keyboard.add(secondRow);

        return keyboard;
    }

    public static List<KeyboardRow> getCreatePostKeyboard() {
        return getTwoRowsKeyboard(STATS_STRING, CREATE_POST_STRING);
    }

    // main

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);

            telegramBotsApi.registerBot(new Main());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
