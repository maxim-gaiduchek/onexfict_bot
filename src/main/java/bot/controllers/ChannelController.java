package bot.controllers;

import bot.entities.Post;
import bot.utils.SimpleSender;

public class ChannelController {

    private static final String CHANNEL_ID = "-1001586043042";

    private ChannelController() {
    }

    public static void post(Post post, SimpleSender sender) {
        if (post.isNotPosted()) {
            Controller.send(post, sender, CHANNEL_ID, "❤️ " + post.getLikesCount(), "post-like");
            post.setPosted();
        }
    }

    public static void editPostLikesKeyboard(Post post, SimpleSender sender, Integer messageId) {
        Controller.editInlineKeyboard(post, sender, CHANNEL_ID, "❤️ " + post.getLikesCount(), "post-like", messageId);
    }
}
