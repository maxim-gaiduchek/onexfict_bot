package bot.controllers;

import bot.entities.Post;
import bot.utils.SimpleSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

class Controller {

    private Controller() {
    }

    static Integer send(Post post, SimpleSender sender, String chatId) {
        try {
            List<String> fileIds = post.getImagesFilesIds();
            Message message;

            if (fileIds.size() >= 2) {
                SendMediaGroup send = new SendMediaGroup();

                send.setChatId(chatId);
                send.setMedias(fileIds.stream().map(fileString -> {
                    String fileId = fileString.substring(fileString.indexOf(':') + 1);

                    return switch (fileString.substring(0, fileString.indexOf(':'))) {
                        case "photo" -> new InputMediaPhoto(fileId);
                        case "video" -> new InputMediaVideo(fileId);
                        default -> null;
                    };
                }).toList());
                send.getMedias().get(0).setCaption(post.getPostText());

                message = sender.execute(send).get(0);
                if (AdminController.ADMIN_CHAT_ID.equals(chatId)) {
                    message = sender.sendString(chatId, "Оцінюйте, панове", message.getMessageId());
                }
            } else {
                String fileIdString = fileIds.get(0);
                String fileId = fileIdString.substring(fileIdString.indexOf(':') + 1);

                if (fileIdString.startsWith("photo:")) {
                    message = sender.sendPhoto(chatId, fileId, post.getPostText());
                } else { // video
                    message = sender.sendVideo(chatId, fileId, post.getPostText());
                }
            }

            return message.getMessageId();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return null;
    }

    static void editInlineKeyboard(List<List<InlineKeyboardButton>> keyboard,
                                   SimpleSender sender, String chatId, Integer messageId) {
        try {
            EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

            markup.setKeyboard(keyboard);

            edit.setChatId(chatId);
            edit.setMessageId(messageId);
            edit.setReplyMarkup(markup);

            sender.execute(edit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
