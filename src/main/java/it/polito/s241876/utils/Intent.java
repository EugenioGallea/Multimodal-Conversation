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
    public static final int BENVENUTO = 1;
    public static final int UTILIZZO = 2;
    public static final int PRESENZA = 3;
    public static final int ACCESSORI = 4;
    public static final int POSIZIONE = 5;
    public static final int RISPOSTA_POSITIVA = 6;
    public static final int RISPOSTA_NEGATIVA = 7;
    public static final int OGGETTO = 8;
    public static final int ABBANDONA = 9;

    private static int value;

    private Intent(int v) {
        Intent.value = v;
    }

    static void setType(int intent) {
        Intent.value = intent;
    }

    public static List<Intent> values() {
        return Arrays.asList(
                new Intent(BENVENUTO),
                new Intent(UTILIZZO),
                new Intent(PRESENZA),
                new Intent(ACCESSORI)
        );
    }
}
