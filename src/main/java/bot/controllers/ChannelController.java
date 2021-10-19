package bot.controllers;

import bot.entities.Post;
import bot.utils.SimpleSender;

public class ChannelController {

    private static final String CHANNEL_ID = "-1001586043042";

    private ChannelController() {
    }

    public static Integer post(Post post, SimpleSender sender) {
        if (post.isNotPosted()) {
            Integer postId = Controller.send(post, sender, CHANNEL_ID, "❤️ " + post.getLikesCount(), "post-like");
            post.setPosted();

            return postId;
        }

        return null;
    }

    public static void editPostLikesKeyboard(Post post, SimpleSender sender, Integer messageId) {
        Controller.editInlineKeyboard(post, sender, CHANNEL_ID, "❤️ " + post.getLikesCount(), "post-like", messageId);
    }
}
