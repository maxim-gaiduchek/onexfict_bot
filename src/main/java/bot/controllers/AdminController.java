package bot.controllers;

import bot.entities.Post;
import bot.utils.Formatter;
import bot.utils.SimpleSender;
import org.telegram.telegrambots.meta.api.objects.User;

public class AdminController {

    public static final String ADMIN_CHAT_ID = "-1001523866129";
    public static final int ADMIN_LIKES = 4;

    private AdminController() {
    }

    public static void sendToAdmin(Post post, User user, SimpleSender sender) {
        String msg = "Мем от [" + Formatter.formatTelegramText(user.getFirstName()) + "](tg://user?id=" + user.getId() + ")";

        sender.sendString(ADMIN_CHAT_ID, msg);
        Controller.send(post, sender, ADMIN_CHAT_ID, "👍 " + post.getAgreesCount(), "admin-agree");
    }

    public static void editAdminAgreeKeyboard(Post post, SimpleSender sender, Integer messageId) {
        Controller.editInlineKeyboard(post, sender, ADMIN_CHAT_ID, "👍 " + post.getAgreesCount(), "admin-agree", messageId);
    }
}
