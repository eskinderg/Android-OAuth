package app.mynote.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public  class AppDate {
    public static String Now(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = new Date(TimeManager.getInstance().getServerTime());
        return dateFormat.format(date);
    }
}
