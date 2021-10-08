package controllers;

import entities.BotUser;
import entities.Post;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import utils.SimpleSender;

import java.util.ArrayList;
import java.util.List;

public class PostsCreator {

    private PostsCreator() {}

    public static void sendAddPhoto(SimpleSender sender, BotUser user) {
        String msg = """
                Скиньте фото мема""";

        user.setStatus(BotUser.Status.IS_ADDING_PHOTO);
        user.setPost(new Post());
        sender.sendString(user.getChatId(), msg);
    }

    public static void addPhoto(SimpleSender sender, BotUser user, String fileId) {
        String msg = """
                Скиньте еще фото мема
                
                /stop - прекратить добавление""";

        user.setStatus(BotUser.Status.IS_ADDING_PHOTO);
        user.getPost().addImageFileId(fileId);
        sender.sendString(user.getChatId(), msg);
    }

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
}
