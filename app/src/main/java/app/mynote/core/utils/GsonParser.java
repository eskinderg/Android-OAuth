package app.mynote.core.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonParser {
    private static Gson gson;

    public static Gson getGsonParser() {
        if (null == gson) {
            GsonBuilder builder = new GsonBuilder();
            gson = builder.create();
        }
        return gson;
    }
}
