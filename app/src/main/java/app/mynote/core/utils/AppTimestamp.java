package app.mynote.core.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppTimestamp {
  public static Timestamp convertStringToTimestamp(String strDate) {
    try {
      DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       // you can change format of date
      Date date = formatter.parse(strDate);
      Timestamp timeStampDate = new Timestamp(date.getTime());

      return timeStampDate;
    } catch (ParseException e) {
      System.out.println("Exception :" + e);
      return null;
    }
  }
}