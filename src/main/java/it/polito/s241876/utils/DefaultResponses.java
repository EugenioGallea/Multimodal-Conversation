package it.polito.s241876.utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Classe che contiene le risposte di default allocate staticamente. Fornisce due metodi:
 * uno per prendere un response a caso e l'altro per prenderne uno specifico.
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 */
public class DefaultResponses {
    // Vettore di stringhe che sono i response di default, nel caso non si capisca
    // qual è l'intento dell'utente.
    final static String[] rispDefault = {
            "Non ho capito, mi spiace...",
            "Non è che potresti ripetere?",
            "Credo di non essere in grado di capire questa cosa...",
    };

    final static String[] rispWelcome = {
            "Ehi, posso fare qualcosa per te?",
            "Buongiorno, posso esserti utile?",
            "Ciao, come posso aiutarti?",
    };

    public static String getOneRandomResponse(){
        int randomNum = ThreadLocalRandom.current().nextInt(0, 2 + 1);
        return rispDefault[randomNum];
    }

    public static String getOneSpecificResponse(int index){
        return rispDefault[index];
    }

    public static String getWelcomeResponse(){
        int randomNum = ThreadLocalRandom.current().nextInt(0, 2 + 1);
        return rispWelcome[randomNum];
    }
}
