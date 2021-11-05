package bot.controllers;

import bot.entities.Post;
import bot.utils.SimpleSender;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class ChannelController {

    public static final String CHANNEL_ID = "-1001586043042";

    private ChannelController() {
    }

    public static Integer post(Post post, SimpleSender sender) {
        if (post.isNotPosted()) {
            Integer postId = Controller.send(post, sender, CHANNEL_ID);
            createPostLikesKeyboard(post, sender, postId);

            post.setPosted(postId);

            return postId;
        }

        return null;
    }

    public static void createPostLikesKeyboard(Post post, SimpleSender sender, Integer postId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(InlineKeyboardButton.builder()
                .text("❤️ " + post.getLikesCount())
                .callbackData("post-like_" + post.getId())
                .build());
        keyboard.add(row);

        Controller.editInlineKeyboard(keyboard, sender, CHANNEL_ID, postId);
    }

    public static void editPostLikesKeyboard(Post post, SimpleSender sender) {
        Integer groupMessageId = post.getGroupMessageId();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        if (groupMessageId != null) {
            row.add(InlineKeyboardButton.builder()
                    .text("\uD83D\uDCAC" + (post.getCommentsCount() > 0 ? (" " + post.getCommentsCount()) : ""))
                    .url("https://t.me/onexfict_chat/" + ((long) groupMessageId + 1000000L) + "?thread=" + groupMessageId)
                    .build());
        }
        row.add(InlineKeyboardButton.builder()
                .text("❤️ " + post.getLikesCount())
                .callbackData("post-like_" + post.getId())
                .build());
        keyboard.add(row);

        Controller.editInlineKeyboard(keyboard, sender, CHANNEL_ID, post.getChannelMessageId());
    }
}
