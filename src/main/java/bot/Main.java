package bot;

import bot.controllers.AdminController;
import bot.controllers.ChannelController;
import bot.controllers.PostsCreator;
import bot.datasource.DatasourceConfig;
import bot.datasource.services.DBService;
import bot.entities.BotUser;
import bot.entities.Post;
import bot.entities.Statistic;
import bot.utils.Formatter;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
        new Executor().start();
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
        Long chatId = message.getChatId();

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
        } else if (message.isGroupMessage() || message.isSuperGroupMessage()) {
            if (chatId.toString().equals(AdminController.ADMIN_CHAT_ID)) {
                parseAdminMessage(message);
            } else {
                sender.leaveChat(chatId);
            }
        } else if (message.getNewChatMembers() != null) {
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
                👋 Это предложка 1xФИВТ (@onexfict). Тут можно предложить мем или новость""";

        sender.sendStringAndKeyboard(chatId, msg, getCreatePostKeyboard(), true);
    }

    private void statsCommand(Long chatId) {
        BotUser user = service.getUser(chatId);

        int allPosts = user.getCreatedPostsIds().size();
        int allLikes = service.getLikesSum(user);
        float allLikesPerPost = service.getLikesPerPost(user);

        int lastLikes = service.get10LastPostsLikesSum(user);
        float lastLikesPerPost = service.get10LastPostsLikesPerPost(user);

        String allTopPostsString = getTop(service.getPostedPostsTop(user));
        String allTopLikesString = getTop(service.getLikesTop(user));
        String allTopLikesPerPostString;

        String lastTopLikesString = getTop(service.get10LastPostsLikesTop(user));
        String lastTopLikesPerPostString;

        if (allPosts >= 5) {
            allTopLikesPerPostString = getTop(service.getLikesPerPostTop(user));
            lastTopLikesPerPostString = getTop(service.get10LastPostsLikesPerPostTop(user));
        } else {
            String numeral = Formatter.formatNumeralText(5 - allPosts, "пост", "поста", "постов");
            allTopLikesPerPostString = " (надо еще " + numeral + " для открытия топа)";
            lastTopLikesPerPostString = " (надо еще " + numeral + " для открытия топа)";
        }

        String msg = "\uD83D\uDCCA *Твоя общая статистика*\n" +
                "\n" +
                "📃 Постов запостили: *" + allPosts + "*" + allTopPostsString + "\n" +
                "❤️ Лайков всего: *" + allLikes + "*" + allTopLikesString + "\n" +
                "\uD83D\uDC65 Лайков за пост в среднем: *" + allLikesPerPost + "*" + allTopLikesPerPostString + "\n" +
                "\n" +
                "\uD83D\uDCCA *Твоя статистика за 10 последних постов*\n" +
                "\n" +
                "❤️ Лайков: *" + lastLikes + "*" + lastTopLikesString + "\n" +
                "\uD83D\uDC65 Лайков за пост в среднем: *" + lastLikesPerPost + "*" + lastTopLikesPerPostString;

        sender.sendStringAndKeyboard(chatId, msg, getCreatePostKeyboard(), true);
    }

    private String getTop(int top) {
        return switch (top) {
            case 1 -> " (Топ 1\uD83E\uDD47)";
            case 2 -> " (Топ 2\uD83E\uDD48)";
            case 3 -> " (Топ 3\uD83E\uDD49)";
            default -> " (Топ " + top + ")";
        };
    }

    private void helpCommand(Long chatId) {
        String msg = """
                ❓ Это предложка 1xФИВТ (@onexfict).
                                
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
            case IS_ADDING_SOURCE -> PostsCreator.sendAddSource(sender, chatId);
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
                if (text.equals(PostsCreator.SKIP_ADDING_TEXT_STRING) || text.startsWith("https://")) {
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
        String callbackQueryId = callbackQuery.getId();
        Message message = callbackQuery.getMessage();
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();
        Long userId = callbackQuery.getFrom().getId();

        String data = callbackQuery.getData();
        String query = data.substring(0, data.indexOf('_'));
        String text = data.substring(data.indexOf('_') + 1);

        Post post = service.getPostById(Long.parseLong(text));

        switch (query) {
            case "admin-agree" -> {
                if (post.switchAgree(userId)) {
                    sender.answerCallbackQuery(callbackQueryId, "Вы одобрили пост 👍");
                } else {
                    sender.answerCallbackQuery(callbackQueryId, "Вы убрали одобрение поста 👎");
                }

                AdminController.editAdminAgreeKeyboard(post, sender, messageId);
                if (post.getAgreesCount() >= AdminController.ADMIN_LIKES) {
                    String adminMgs = "Пост подтвержден " + post.getWhoHasAgreed() + " и запостен. *Поставьте реакции на посте на канале!*";
                    Integer postId = ChannelController.post(post, sender);

                    sender.removeKeyboard(chatId, messageId);
                    sender.unpinMessage(chatId, messageId);
                    sender.sendString(chatId, adminMgs, messageId);

                    if (postId != null) {
                        String msg = "[Пост](https://t.me/onexfict/" + postId + ") подтвержден и опубликован. Спасибо за поддержку❤️";

                        sender.sendString(post.getCreatorId(), msg);
                    }
                }
            }
            case "post-like" -> {
                boolean hasLiked = post.switchLike(userId);

                new Thread(() -> {
                    ChannelController.editPostLikesKeyboard(post, sender);

                    if (hasLiked) {
                        sender.answerCallbackQuery(callbackQueryId, "Вы поставили лайк ❤️");
                    } else {
                        sender.answerCallbackQuery(callbackQueryId, "Вы убрали лайк поста 😔");
                    }
                }).start();
            }
        }

        service.savePost(post);
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

    // admin message parsing

    private void parseAdminMessage(Message message) {
        if (message.isCommand()) {
            String command = message.getText();

            if (command.equals("/stats") || command.equals("/stats@" + BOT_USERNAME)) {
                sendAdminStats();
            }
        }
    }

    private void sendAdminStats() {
        Statistic yesterday = service.getYesterdayStatistics();
        Statistic today = service.getTodayStatistics();

        int posts = today.getPosts();
        int likes = today.getLikes();
        int subscribers = today.getSubscribers();
        float likesPerPost = today.getLikesPerPost();

        int postsToday = posts - yesterday.getPosts();
        int likesToday = likes - yesterday.getLikes();
        int subscribersToday = subscribers - yesterday.getSubscribers();

        DateFormat format = new SimpleDateFormat("_На состояние dd.MM.yyyy HH:mm_");

        format.setTimeZone(TimeZone.getTimeZone("Europe/Kiev"));

        String msg = "\uD83D\uDCCA *Статистика канала*\n" +
                "\n" +
                "📃 Постов запостили: *" + posts + "* (" + (postsToday > 0 ? "+" : "") + postsToday + " за сегодня)\n" +
                "❤️ Лайков всего: *" + likes + "* (" + (likesToday > 0 ? "+" : "") + likesToday + " за сегодня)\n" +
                "\uD83D\uDC65 Подписчиков всего: *" + subscribers + "* (" + (subscribersToday > 0 ? "+" : "") + subscribersToday + " за сегодня)\n" +
                "\uD83D\uDCC8 Лайков за пост в среднем: *" + likesPerPost + "*\n" +
                "\n" +
                format.format(new Date());

        sender.sendString(AdminController.ADMIN_CHAT_ID, msg);
    }

    // executor

    private class Executor extends Thread {

        private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

        static {
            TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("Europe/Kiev"));
        }

        private Executor() {
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String time = TIME_FORMAT.format(new Date());

                    switch (time) {
                        case "00:00" -> service.createNewStatisticsEntity();
                        case "22:00" -> sendAdminStats();
                        default -> service.updateStatistics();
                    }

                    sleep(60000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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
