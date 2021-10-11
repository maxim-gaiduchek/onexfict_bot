package bot.controllers;

import bot.Main;
import bot.entities.BotUser;
import bot.entities.Post;
import bot.utils.SimpleSender;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class PostsCreator {

    public static final String STOP_ADDING_PHOTO_STRING = "Остановить добавление";

    private PostsCreator() {
    }

    // media

    public static void sendAddPhoto(SimpleSender sender, Long chatId) {
        sender.sendStringAndRemoveKeyboard(chatId, "Скиньте мем");
    }

    public static void sendAddPhoto(SimpleSender sender, BotUser user) {
        user.setStatus(BotUser.Status.IS_ADDING_PHOTO);
        user.setPost(new Post());
        sendAddPhoto(sender, user.getChatId());
    }

    public static void addPhoto(SimpleSender sender, Long chatId) {
        String msg = """
                Скиньте продолжение мема или еще один мем""";

        sender.sendStringAndKeyboard(chatId, msg, getAddPhotoKeyboard(), true);
    }

    public static void addPhoto(SimpleSender sender, BotUser user, String fileId) {
        user.setStatus(BotUser.Status.IS_ADDING_PHOTO);
        user.getPost().addImageFileId(fileId);
        addPhoto(sender, user.getChatId());
    }

    // text

    public static void sendAddText(SimpleSender sender, Long chatId) {
        String msg = """
                Введите текст к мему, который будет под фотками в посте""";

        sender.sendString(chatId, msg);
    }

    public static void sendAddText(SimpleSender sender, BotUser user) {
        user.setStatus(BotUser.Status.IS_ADDING_TEXT);
        sendAddText(sender, user.getChatId());
    }

    public static void addText(SimpleSender sender, BotUser user, String text) {
        user.getPost().setText(text);
        sendAddBy(sender, user);
    }

    // by

    public static void sendAddBy(SimpleSender sender, Long chatId) {
        String msg = """
                Введите, от кого этот мем (под мемом будет отображено "by <имя>")""";

        sender.sendString(chatId, msg);
    }

    public static void sendAddBy(SimpleSender sender, BotUser user) {
        user.setStatus(BotUser.Status.IS_ADDING_BY);
        sendAddText(sender, user.getChatId());
    }

    public static void addBy(SimpleSender sender, BotUser user, String by) {
        String msg = """
                Спасибо за мемес. Его проверят админы и запостят на канал""";

        if (!by.startsWith("by ")) by = "by " + by;

        user.setStatus(BotUser.Status.INACTIVE);
        user.getPost().setBy(by);
        sender.sendStringAndKeyboard(user.getChatId(), msg, Main.getCreatePostKeyboard(), true);
    }

    // source

    // keyboards

    private static List<KeyboardRow> getAddPhotoKeyboard() {
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add(STOP_ADDING_PHOTO_STRING);
        keyboard.add(row);

        return keyboard;
    }
}
