package com.example.ogima.helper;

public class FriendshipLevelUtils {

    private static final String FRL_0 = "Ternura";
    private static final String FRL_10 = "Teste";

    public static String adjustFriendshipLevel(int totalMessages) {
        if (totalMessages == 0) {
            return FRL_0;
        } else if (totalMessages >= 10) {
            return FRL_10;
        }
        return FRL_0;
    }
}
