package it.polito.s241876.utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Classe che contiene le risposte di default allocate staticamente. Fornisce due metodi:
 * uno per prendere un response a caso e l'altro per prenderne uno specifico.
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 */
public class Response {
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

    final static String[] rispOfferTask = {
            "Preferisci che faccia io?",
            "Se vuoi posso fare io, va bene?",
            "Posso farlo io se preferisci, ti va?"
    };

    final static String[] rispPosition = {
            "Quali?"
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

    public static String getOneRandomOfferResponse() {
        int randomNum = ThreadLocalRandom.current().nextInt(0, 2 + 1);
        return rispOfferTask[randomNum];
    }

    public static String getOneRandomPositionResponse(String oggetto) {
        // TODO: si può determinare se una parola è singolare o plurale, maschile o femminile?
        return rispPosition[0];
    }

    public static String getOneAboutObjectResponse(String oggetto) {
        return "Di cosa avresti bisogno a proposito dell'accessorio: " + oggetto;
    }

    public static String getListaAccessoriResponse(List<Accessorio> a_list) {
        String response = "Eccoti l'elenco di cosa è presente in macchina: ";
        int i = 0;
        for (Accessorio a : a_list) {
            response += a.getNome();
            i++;
            if (i != a_list.size())
                response += ", ";
        }

        return response;
    }

    public static String getIstruzioniUsoOggettoResponse(String oggetto, String istr, boolean exists) {
        String response = "";
        if (exists)
            response = "Le istruzioni d'uso per l'accessorio \"" + oggetto + "\" sono: " + "\n" + istr;
        else
            response += "Non conosco il funzionamento dell'oggetto \""
                    + oggetto + "\"";

        return response;
    }

    public static String getPresenzaOggettoResponse(String oggetto, boolean exists) {
        String response = "";
        if (exists)
            response += "L'oggetto \"" + oggetto + "\" è presente su questo veicolo.";
        else
            response += "L'oggetto \"" + oggetto + "\" non è presente su questo veicolo.";

        return response;
    }

    public static String getErrorActionInterpretation() {
        return "Errore nell'interpretazione dell'azione";
    }

    public static String getDismissResponse() {
        return "Va bene, chiedi se hai bisogno di altro.";
    }

    public static String getNoTaskExecutingResponse() {
        return "Non sto eseguendo nulla...";
    }

    public static String getAccensioneAccessorioResponse(String oggetto, String istr, boolean positionSpecified) {
        if (positionSpecified)
            return istr + " \n" + getOneRandomOfferResponse();
        else
            return "Dovresti specificare la posizione dell'oggetto " + oggetto;
    }
}
