package bot.controllers;

import bot.entities.Post;
import bot.utils.SimpleSender;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class ChannelController {

    private static final String CHANNEL_ID = "-1001586043042";

    private ChannelController() {
    }

    public static Integer post(Post post, SimpleSender sender) {
        if (post.isNotPosted()) {
            Integer postId = Controller.send(post, sender, CHANNEL_ID);
            editPostLikesKeyboard(post, sender, postId);

            post.setPosted();

            return postId;
        }

        return null;
    }

    public static void editPostLikesKeyboard(Post post, SimpleSender sender, Integer messageId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        /*row.add(InlineKeyboardButton.builder()
                .text("\uD83D\uDCAC")
                .url("https://t.me/onexfict_chat?thread=" + messageId)
                .build());*/
        row.add(InlineKeyboardButton.builder()
                .text("❤️ " + post.getLikesCount())
                .callbackData("post-like_" + post.getId())
                .build());
        keyboard.add(row);

        Controller.editInlineKeyboard(keyboard, sender, CHANNEL_ID, messageId);
    }
}
