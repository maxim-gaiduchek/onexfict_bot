package bot.controllers;

import bot.Main;
import bot.entities.BotUser;
import bot.entities.Post;
import bot.utils.SimpleSender;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class PostsCreator {

    public static final String STOP_ADDING_PHOTO_STRING = "❌ Остановить добавление";
    public static final String STOP_CREATING_POST_STRING = "❌ Остановить создание поста";
    private static final String SKIP_ADDING_TEXT_STRING = "❌ Пропустить этот шаг";

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

        sender.sendStringAndKeyboard(chatId, msg, getSkipStepKeyboard(), true);
    }

    public static void sendAddText(SimpleSender sender, BotUser user) {
        user.setStatus(BotUser.Status.IS_ADDING_TEXT);
        sendAddText(sender, user.getChatId());
    }

    public static void addText(SimpleSender sender, BotUser user, String text) {
        if (!text.equals(SKIP_ADDING_TEXT_STRING)) {
            user.getPost().setText(text);
        }
        sendAddBy(sender, user);
    }

    // by

    public static void sendAddBy(SimpleSender sender, Long chatId) {
        String msg = """
                Введите, от кого этот мем (под мемом будет отображено "by <имя>")""";

        sender.sendStringAndKeyboard(chatId, msg, getSkipStepKeyboard(), true);
    }

    public static void sendAddBy(SimpleSender sender, BotUser user) {
        user.setStatus(BotUser.Status.IS_ADDING_BY);
        sendAddBy(sender, user.getChatId());
    }

    public static void addBy(SimpleSender sender, BotUser user, String by) {
        if (!by.equals(SKIP_ADDING_TEXT_STRING)) {
            user.getPost().setBy(by);
        }
        sendAddSource(sender, user);
    }

    // source

    public static void sendAddSource(SimpleSender sender, Long chatId) {
        String msg = """
                Введите источник к этому мему (если надо)""";

        sender.sendStringAndKeyboard(chatId, msg, getSkipStepKeyboard(), true);
    }

    public static void sendSourceError(SimpleSender sender, Long chatId) {
        String msg = """
                Ссылка должна начинаться на "https://\"""";

        sender.sendStringAndKeyboard(chatId, msg, getSkipStepKeyboard(), true);
    }

    public static void sendAddSource(SimpleSender sender, BotUser user) {
        user.setStatus(BotUser.Status.IS_ADDING_SOURCE);
        sendAddSource(sender, user.getChatId());
    }

    public static void addSource(SimpleSender sender, BotUser user, String source) {
        String msg = """
                    Спасибо за мемес. Его проверят админы и запостят на канал""";

        if (!source.equals(SKIP_ADDING_TEXT_STRING)) {
            user.getPost().setSource(source);
        }

        user.setStatus(BotUser.Status.INACTIVE);
        sender.sendStringAndKeyboard(user.getChatId(), msg, Main.getCreatePostKeyboard(), true);
    }

    // keyboards

    private static List<KeyboardRow> getAddPhotoKeyboard() {
        return getKeyboard(STOP_ADDING_PHOTO_STRING);
    }

    private static List<KeyboardRow> getSkipStepKeyboard() {
        return getKeyboard(SKIP_ADDING_TEXT_STRING);
    }

    private static List<KeyboardRow> getKeyboard(String str) {
        return Main.getTwoRowsKeyboard(STOP_CREATING_POST_STRING, str);
    }
}
