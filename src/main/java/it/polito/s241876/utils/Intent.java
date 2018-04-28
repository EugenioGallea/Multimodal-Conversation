package it.polito.s241876.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Questa classe è pensata per avere da qualche parte un enum nel quale tenere il significato degli intenti, essendo
 * che da DialogFlow sono pensati arrivare sotto forma di interi.
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 */
public class Intent {
    public static final int WELCOME = 1;
    public static final int FUNCTIONING = 2;
    public static final int PRESENCE = 3;
    public static final int ALL_ACESSORIES = 4;
    public static final int POSITION_ANSWER = 5;
    public static final int AFFERMATIVE_ANSWER = 6;
    public static final int NEGATIVE_ANSWER = 7;

    private static int value;

    private Intent(int v) {
        Intent.value = v;
    }

    static void setType(int intent) {
        Intent.value = intent;
    }

    public static List<Intent> values() {
        return Arrays.asList(
                new Intent(WELCOME),
                new Intent(FUNCTIONING),
                new Intent(PRESENCE),
                new Intent(ALL_ACESSORIES)
        );
    }
}
