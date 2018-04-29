package it.polito.s241876.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Nel caso ci fossero delle costanti (indici json, path,...) questa è la classe dove aggiungerle.
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 */
public class Constants {
    public static boolean RUNNING = true;

    public static final String API_KEY = "b440b5389a3449bc8a89f8f5c78296a8";

    public static final String CONTENT_TYPE = "application/json";
    public static final String PATH = "/";

    public static final String OBJECT = "oggetto";
    public static final String ACTION = "azione";
    public static final String POSITION = "posizione";
    public static final String QUANTITY = "numero";

    public static final String INSTRUCTIONS = "istruzioni_uso";
    public static final String ID = "id";

    public enum IntentType {
        WELCOME(1, "welcome"), FUNCTIONING(2, "functioning"), PRESENCE(3, "presence"),
        ALL_ACCESSORIES(4, "all accessories"), POSITION_ANSWER(5, "position answer"), AFFIRMATIVE_ANSWER(6, "affermative answer"),
        NEGATIVE_ANSWER(7, "negative answer"), SIMPLE_OBJECT(8, "simple object");

        private static Map<Integer, IntentType> map = new HashMap<>();

        static {
            for (IntentType intentType : IntentType.values()) {
                map.put(intentType.intentNo, intentType);
            }
        }

        public final int intentNo;
        private String description;

        IntentType(final int intent, String desc) {
            this.intentNo = intent;
            this.description = desc;
        }

        public static IntentType valueOf(int intentNo) {
            return map.get(intentNo);
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String descr) {
            this.description = descr;
        }

        public int getIntentNo() {
            return intentNo;
        }
    }
}
