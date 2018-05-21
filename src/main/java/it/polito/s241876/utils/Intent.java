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
    public static final int UTILIZZO = 2;
    public static final int PRESENZA = 3;
    public static final int ACCESSORI = 4;
    public static final int POSIZIONE = 5;
    public static final int RISPOSTA = 6;
    public static final int LISTA_POSIZIONI_OGGETTO = 7;

    private static int value;

    private Intent(int v) {
        Intent.value = v;
    }

    static void setType(int intent) {
        Intent.value = intent;
    }

    public static List<Intent> values() {
        return Arrays.asList(
                new Intent(UTILIZZO),
                new Intent(PRESENZA),
                new Intent(ACCESSORI),
                new Intent(POSIZIONE),
                new Intent(RISPOSTA),
                new Intent(LISTA_POSIZIONI_OGGETTO)
        );
    }
}
