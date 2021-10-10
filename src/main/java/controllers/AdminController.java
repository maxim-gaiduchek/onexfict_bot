package controllers;

import entities.Post;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.Formatter;
import utils.SimpleSender;

import java.util.ArrayList;
import java.util.List;

public class AdminController {

    public static final String ADMIN_CHAT_ID = "-1001523866129";
    public static final int ADMIN_LIKES = 3;

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
