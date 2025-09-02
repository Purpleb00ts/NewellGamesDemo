package app.newellgames.utility;

import java.util.UUID;

public class UuidUtility {

    public static boolean isValid(String uuidString) {
        if (uuidString == null) return false;
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
