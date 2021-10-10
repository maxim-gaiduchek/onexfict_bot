package controllers;

import entities.Post;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.SimpleSender;

import java.util.ArrayList;
import java.util.List;

class Controller {

    private Controller() {}

    static void send(Post post, SimpleSender sender, String chatId, String text, String query) {
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
                if (post.hasText()) {
                    send.getMedias().get(0).setCaption(post.getText());
                }

                message = sender.execute(send).get(0);
            } else {
                String fileId = fileIds.get(0);
                InputFile file = new InputFile(fileId.substring(fileId.indexOf(':') + 1));

                if (fileId.startsWith("photo:")) {
                    SendPhoto send = new SendPhoto();

                    send.setChatId(chatId);
                    send.setPhoto(file);
                    if (post.hasText()) send.setCaption(post.getText());

                    message = sender.execute(send);
                } else { // video
                    SendVideo send = new SendVideo();

                    send.setChatId(chatId);
                    send.setVideo(file);
                    if (post.hasText()) send.setCaption(post.getText());

                    message = sender.execute(send);
                }
            }

            editInlineKeyboard(post, sender, chatId, text, query, message.getMessageId());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    static void editInlineKeyboard(Post post, SimpleSender sender, String chatId, String text, String query, Integer messageId) {
        try {
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();

            row.add(InlineKeyboardButton.builder().text(text).callbackData(query + "_" + post.getId()).build());
            keyboard.add(row);

            EditMessageReplyMarkup edit = new EditMessageReplyMarkup();

            edit.setChatId(chatId);
            edit.setMessageId(messageId);
            edit.setReplyMarkup(new InlineKeyboardMarkup(keyboard));

            sender.execute(edit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
