import controllers.AdminController;
import controllers.ChannelController;
import controllers.PostsCreator;
import datasource.DatasourceConfig;
import datasource.services.DBService;
import entities.BotUser;
import entities.Post;
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
import utils.SimpleSender;

import java.util.ArrayList;
import java.util.List;

public class Main extends TelegramLongPollingBot {

    private static final String BOT_USERNAME = System.getenv("BOT_USERNAME");
    private static final String BOT_TOKEN = System.getenv("BOT_TOKEN");

    private final SimpleSender sender = new SimpleSender(BOT_TOKEN);

    private static final ApplicationContext CONTEXT = new AnnotationConfigApplicationContext(DatasourceConfig.class);
    private final DBService service = (DBService) CONTEXT.getBean("service");

    // start

    private Main() {
        Post post13 = new Post();
        Post post14 = new Post();
        Post post15 = new Post();

        post13.setPosted();
        post14.setPosted();
        post15.setPosted();

        ChannelController.editPostLikesKeyboard(post13, sender, 13);
        ChannelController.editPostLikesKeyboard(post14, sender, 14);
        ChannelController.editPostLikesKeyboard(post15, sender, 15);

        service.savePost(post13);
        service.savePost(post14);
        service.savePost(post15);
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

    private void parseCommand(Message message) {
        Long chatId = message.getChatId();
        BotUser user = service.getUser(chatId);
        String command = message.getText();

        if (user.getStatus() == BotUser.Status.INACTIVE) {
            switch (message.getText()) {
                case "/start" -> startCommand(chatId);
                case "/help" -> helpCommand(chatId);
                case "/post" -> {
                    PostsCreator.sendAddPhoto(sender, user);
                    service.saveUser(user);
                }
            }
        } else if (user.getStatus() == BotUser.Status.IS_ADDING_PHOTO && command.equals("/stop")) {
            PostsCreator.sendAddText(sender, user);
            service.saveUser(user);
        } else {
            sender.deleteMessage(chatId, message.getMessageId());
        }
    }

    // commands

    private void startCommand(Long chatId) {
        String msg = """
                Это предложка 1xФИВТ (@onexfict). Тут можно предложить мем или новость""";

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add("Предложить пост");
        keyboard.add(row);

        sender.sendStringAndKeyboard(chatId, msg, keyboard, true);
    }

    private void helpCommand(Long chatId) {
        String msg = """
                Это предложка 1xФИВТ (@onexfict). Введи /post, чтоб предложить мем""";

        sender.sendString(chatId, msg);
    }

    // photo media

    private void parseMedia(Message message) {
        BotUser user = service.getUser(message.getChatId());

        if (user.getStatus() == BotUser.Status.IS_ADDING_PHOTO) {
            String fileId = null;

            if (message.hasPhoto()) {
                fileId = "photo:" + message.getPhoto().get(0).getFileId();
            } else if (message.hasVideo()) {
                fileId = "video:" + message.getVideo().getFileId();
            }

            PostsCreator.addPhoto(sender, user, fileId);
            service.savePost(user.getPost());
            service.saveUser(user);
        }
    }

    // text parsing

    private void parseTextMessage(Message message) {
        BotUser user = service.getUser(message.getChatId());
        String text = message.getText();

        if (user.getStatus() == BotUser.Status.INACTIVE && text.equals("Предложить пост")) {
            PostsCreator.sendAddPhoto(sender, user);
            service.savePost(user.getPost());
        } else if (user.getStatus() == BotUser.Status.IS_ADDING_TEXT) {
            PostsCreator.addText(sender, user, message.getText());

            AdminController.sendToAdmin(user.getPost(), message.getFrom(), sender);
            service.savePost(user.getPost());
            user.setPost(null);
        }
        service.saveUser(user);
    }

    // parse callback query

    private void parseCallbackQuery(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
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
