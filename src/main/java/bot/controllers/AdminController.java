package bot.controllers;

import bot.entities.Post;
import bot.utils.Formatter;
import bot.utils.SimpleSender;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class AdminController {

    public static final String ADMIN_CHAT_ID = "-1001523866129";
    public static final int ADMIN_LIKES = 4;

    private AdminController() {
    }

    public static void sendToAdmin(Post post, User user, SimpleSender sender) {
        String msg = "Мем от [" + Formatter.formatTelegramText(user.getFirstName()) + "](tg://user?id=" + user.getId() + ")";

        sender.sendString(ADMIN_CHAT_ID, msg);
        Integer messageId = Controller.send(post, sender, ADMIN_CHAT_ID);
        editAdminAgreeKeyboard(post, sender, messageId);
        sender.pinMessage(ADMIN_CHAT_ID, messageId, false);
    }

    public static void editAdminAgreeKeyboard(Post post, SimpleSender sender, Integer messageId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(InlineKeyboardButton.builder()
                .text("👍 " + post.getAgreesCount())
                .callbackData("admin-agree_" + post.getId())
                .build());
        keyboard.add(row);

        Controller.editInlineKeyboard(keyboard, sender, ADMIN_CHAT_ID, messageId);
    }
}
