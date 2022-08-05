package bot.controllers;

import bot.Main;
import bot.entities.BotUser;
import bot.entities.Post;
import bot.utils.SimpleSender;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

public class PostsCreator {

    public static final String STOP_ADDING_PHOTO_STRING = "❌ Зупинити додавання";
    public static final String STOP_CREATING_POST_STRING = "❌ Зупинити створення посту";
    public static final String SKIP_ADDING_TEXT_STRING = "❌ Пропустити цей крок";

    private PostsCreator() {
    }

    // media

    public static void sendAddPhoto(SimpleSender sender, Long chatId) {
        sender.sendStringAndKeyboard(chatId, "\uD83D\uDDBC Скиньте мем", getSkipStepKeyboard());
    }

    public static void sendAddPhoto(SimpleSender sender, BotUser user) {
        user.setStatus(BotUser.Status.IS_ADDING_PHOTO);
        user.setPost(new Post(user));
        sendAddPhoto(sender, user.getChatId());
    }

    public static void addPhoto(SimpleSender sender, Long chatId) {
        String msg = """
                🖼 Скиньте продовження мема або ще один мем""";

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
                \uD83D\uDCC3 Введіть текст до мему, який буде під фотками у пості""";

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
                \uD83D\uDC65 Введіть, від кого цей мем (під мемом буде відображено "by <ім'я>")""";

        sender.sendStringAndKeyboard(chatId, msg, getSkipStepKeyboard(), true);
    }

    public static void sendAddBy(SimpleSender sender, BotUser user) {
        user.setStatus(BotUser.Status.IS_ADDING_BY);
        sendAddBy(sender, user.getChatId());
    }

    public static void addBy(SimpleSender sender, BotUser user, String by) {
        if (!by.equals(SKIP_ADDING_TEXT_STRING)) {
            if (by.startsWith("by ")) by = by.substring(3);

            user.getPost().setBy(by);
        }
        sendAddSource(sender, user);
    }

    // source

    public static void sendAddSource(SimpleSender sender, Long chatId) {
        String msg = """
                🌐 Введіть джерело до цього мему (якщо треба)""";

        sender.sendStringAndKeyboard(chatId, msg, getSkipStepKeyboard(), true);
    }

    public static void sendSourceError(SimpleSender sender, Long chatId) {
        String msg = """
                🌐 Посилання має починатися на "https://\"""";

        sender.sendStringAndKeyboard(chatId, msg, getSkipStepKeyboard(), true);
    }

    public static void sendAddSource(SimpleSender sender, BotUser user) {
        user.setStatus(BotUser.Status.IS_ADDING_SOURCE);
        sendAddSource(sender, user.getChatId());
    }

    public static void addSource(SimpleSender sender, BotUser user, String source) {
        String msg = """
                👍 Дякую за мемес. Його перевірять адміни та запостіть на канал""";

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
