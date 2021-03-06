package bot.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Formatter {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Europe/Kiev"));
    }

    private Formatter() {
    }

    public static Date formatDate(Date date) {
        try {
            return new Date(DATE_FORMAT.parse(DATE_FORMAT.format(date)).getTime() + 3 * 60 * 60 * 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    public static String formatTelegramText(String text) {
        return text.replace("*", "\\*")
                .replace("_", "\\_")
                .replace("-", "\\-")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace("[", "\\[")
                .replace("!", "\\!")
                .replace(".", "\\.");
    }

    public static String formatNumeralText(int number, String ending0, String ending1, String ending2) {
        String textToOutput = number + " ";

        return switch (number) {
            case 11, 12, 13, 14 -> textToOutput + ending2;
            default -> switch (number % 10) {
                case 1 -> textToOutput + ending0;
                case 2, 3, 4 -> textToOutput + ending1;
                default -> textToOutput + ending2;
            };
        };
    }

    public static float round(float number, int digits) {
        float dec = (float) Math.pow(10, digits);

        return Math.round(dec * number) / dec;
    }
}
