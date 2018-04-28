package it.polito.s241876.main;

import ai.api.model.AIResponse;
import com.google.gson.Gson;
import ai.api.GsonFactory;
import ai.api.model.Fulfillment;
import it.polito.s241876.database.AccessoriDB;
import it.polito.s241876.utils.*;

import java.util.List;

import static spark.Spark.*;

/**
 * Questo è il codice per un agente conversazionale minimo. Possiede risposte a poche
 * possibili domande che l'utente può chiedere. Nel caso non sia possibile trovare una
 * risposta adeguata, viene ritornata una risposta di default, scelta randomicamente
 * da un pool di risposte di default.
 *
 * ATTENZIONE: se si usa 'ngrok' per avere un URL pubblico, poi va cambiato su DialogFlow.
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 */
public class Main {
    public static final String CONTENT_TYPE = "application/json";

    public static void main(String[] args) {

        // Inizializzo l'oggetto gson che mi permette di parsare correttamente la richiesta
        Gson gson = GsonFactory.getDefaultFactory().getGson();

        // Il path del webhook. L'ho messo nella root perché mi sembrava più facile
        post("/", (request, response) -> {
            Fulfillment output = new Fulfillment();

            // Funzione che chiama la dispatcher. L'ho messa semplicemente nel caso
            // si volesse poi aggiungere dell'altra pre-elaborazione prima.
            doWebhook(gson.fromJson(request.body(), AIResponse.class), output);

            response.type(CONTENT_TYPE);
            response.status(200);

            // L'output è automaticamente trasformato in json
            return output;
        }, gson::toJson);

    }

    private static void doWebhook(AIResponse input, Fulfillment output) {
        // Nel caso si voglia fare della pre-elaborazione, farla qua.
        String response = dispatcherFunction(output, Integer.parseInt(input.getResult().getMetadata().getIntentName()), input);

        // Setto il response nel Json output finale
        output.setSpeech(response);
        output.setDisplayText(response);
    }

    private static String dispatcherFunction(Fulfillment output, int intent, AIResponse input){
        /*
            Questa funzione è quella che discrimina qual è l'intento dell'utente e sceglie cosa
            fare e qual è la funzione adatta per la query al database.
         */

        String response = new String(); // Stringa per il response, verrà poi modificata col risultato della query specifica
        AccessoriDB adb = new AccessoriDB(); // Classe per le interazioni con il DB

        // Questo è lo switch per discriminare l'intendo dell'utente e agire di conseguenza

        switch (intent){
            case Intent.WELCOME: // Welcome user
                response = DefaultResponses.getWelcomeResponse();
                break;

            case Intent.FUNCTIONING: // L'utente ha chiesto come si utilizza un particolare accessorio
                String oggetto2 = input.getResult().getParameters().get("oggetto").getAsString();

                // Pulizia dell'input
                oggetto2.replaceAll("[-+.^:,]","");
                oggetto2.replace("?", "");
                //System.out.println("Oggetto: " + oggetto2);
                boolean exists_2 = adb.doesExist(input.getResult().getParameters().get("oggetto").getAsString());
                String istr = adb.getIstruzioniUso(oggetto2);
                response = ResponseFactory
                            .getIstruzioniUsoOggettoResponse(oggetto2, istr, exists_2);
                break;

            case Intent.PRESENCE: // L'utente ha richiesto se è presente un certo oggetto (accessorio)
                boolean exists_3 = adb.doesExist(input.getResult().getParameters().get("oggetto").getAsString());
                String oggetto3 = input.getResult().getParameters().get("oggetto").getAsString();

                // Pulizia dell'input
                oggetto3.replaceAll("[-+.^:,]","");
                oggetto3.replace("?", "");
                //System.out.println("Oggetto: " + oggetto3);
                response = ResponseFactory.getPresenzaOggettoResponse(oggetto3, exists_3);

                break;

            case Intent.ALL_ACESSORIES: // L'utente ha richiesto la lista degli accessori
                List<Accessorio> a_list = adb.getAllAccessori(); // Lista degli accessori salvati nel db

                // Setto il response nel Json output finale
                response = ResponseFactory.getListaAccessoriResponse(a_list);
                break;

            default: // L'utente ha inserito qualcosa alla quale io non so rispondere
                response = DefaultResponses.getOneRandomResponse(); // Prendo un response di default a caso

                break;
        }

        return response;
    }
}
