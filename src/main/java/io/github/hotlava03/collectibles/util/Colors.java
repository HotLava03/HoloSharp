package io.github.hotlava03.collectibles.util;

public class Colors {

    public static String fixCodes(String txt) {
        return txt.replace("&","\u00a7");
    }
}
