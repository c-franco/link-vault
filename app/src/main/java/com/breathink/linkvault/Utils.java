package com.breathink.linkvault;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static boolean validUrl(String url) {

        Pattern patron = Pattern.compile("^(https?://(w{3}\\.)?)?\\w+\\.\\w+(\\.[a-zA-Z]+)*(:\\d{1,5})?(/\\w*)*(\\??(.+=.*)?(&.+=.*)?)?$");
        Matcher mat = patron.matcher(url);

        return mat.matches();
    }

    public static boolean validString(String string) {

        Pattern patron = Pattern.compile("^(?!\\s*$).+");

        Matcher mat = patron.matcher(string);

        return mat.matches();
    }

    public static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
