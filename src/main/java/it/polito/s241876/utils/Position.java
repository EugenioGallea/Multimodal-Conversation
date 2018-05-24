package it.polito.s241876.utils;

import java.util.HashMap;
import java.util.Map;

public class Position {
    private static final String TAG = "[Position] ";
    private static Map<String, Integer> positionDispatcher = null;

    private static void initializaMap() {
        positionDispatcher = new HashMap<>();
        positionDispatcher.put("anteriore", 1);
        positionDispatcher.put("posteriore", 2);
        positionDispatcher.put("centrale", 3);
        positionDispatcher.put("destra", 4);
        positionDispatcher.put("sinistra", 5);
    }

    public static int getValueGivenString(String key) {
        if (positionDispatcher == null)
            initializaMap();
        try {
            return positionDispatcher.get(key);
        } catch (NullPointerException e) {
            // Niente, mi è stata passata una posizione nulla
            return -1;
        }
    }

    public static String getValueGivenInt(int position) {
        if (positionDispatcher == null)
            initializaMap();

        try {
            for (Map.Entry<String, Integer> entry : positionDispatcher.entrySet()) {
                if (entry.getValue() == position)
                    return entry.getKey();
            }
        } catch (NullPointerException e) {
            return null;
        }

        return null;
    }
}
