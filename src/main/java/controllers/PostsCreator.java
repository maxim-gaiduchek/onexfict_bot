package controllers;

import entities.BotUser;
import entities.Post;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import utils.SimpleSender;

import java.util.ArrayList;
import java.util.List;

public class PostsCreator {

    public static final String STOP_ADDING_PHOTO_STRING = "Остановить добавление";

    private PostsCreator() {}

    // media

    public static void sendAddPhoto(SimpleSender sender, Long chatId) {
        sender.sendString(chatId, "Скиньте мем");
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

    public static void sendAddText(SimpleSender sender, BotUser user) {
        String msg = """
                Напишите, от кого этот мем (под фото мема будет отображено "by <имя>")""";

        user.setStatus(BotUser.Status.IS_ADDING_TEXT);
        sender.sendString(user.getChatId(), msg);
    }

    public static void addText(SimpleSender sender, BotUser user, String text) {
        String msg = """
                Спасибо за мемес. Его проверят админы и запостят на канал""";

        if (!text.startsWith("by ")) text = "by " + text;

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add("Предложить пост");
        keyboard.add(row);

        user.setStatus(BotUser.Status.INACTIVE);
        user.getPost().setText(text);
        sender.sendStringAndKeyboard(user.getChatId(), msg, keyboard, true);
    }

    // from

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
