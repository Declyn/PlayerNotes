package dev.declyn.playernotes.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

    public static String formatDate(long millis) {
        Date date = new Date(millis);
        return new SimpleDateFormat("MMM dd yyyy (hh:mm aa zz)").format(date);
    }

}