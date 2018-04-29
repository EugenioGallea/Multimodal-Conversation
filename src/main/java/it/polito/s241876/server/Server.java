package it.polito.s241876.server;

import ai.api.GsonFactory;
import ai.api.model.AIResponse;
import ai.api.model.Fulfillment;
import com.google.gson.Gson;
import it.polito.s241876.database.AccessoriDB;
import it.polito.s241876.utils.*;
import javafx.util.Pair;

import static spark.Spark.post;

/**
 * Questa è la classe che riceverà le richieste da parte di DialogFlow contenenti ciò che l'assistente
 * conversazionale è riuscito a interpretare
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 */

public class Server {
    private static final String TAG = "[Server] ";
    private static final AccessoriDB adb = AccessoriDB.getInstance(); // Classe per interazione con il database

    public static void entryPoint() {
        // Inizializzo l'oggetto gson che mi permette di parsare correttamente la richiesta
        Gson gson = GsonFactory.getDefaultFactory().getGson();

        // Il path del webhook. L'ho messo nella root perché mi sembrava più facile
        post(Constants.PATH, (request, response) -> {
            Fulfillment output = new Fulfillment();

            // Funzione che chiama la dispatcher. L'ho messa semplicemente nel caso
            // si volesse poi aggiungere dell'altra pre-elaborazione prima.
            doWebhook(gson.fromJson(request.body(), AIResponse.class), output);

            response.type(Constants.CONTENT_TYPE);
            response.status(200);

            // L'output è automaticamente trasformato in json
            return output;
        }, gson::toJson);

    }

    private static void doWebhook(AIResponse input, Fulfillment output) {
        // Nel caso si voglia fare della pre-elaborazione, farla qua.
        String response = dispatcherFunction(Integer.parseInt(input.getResult().getMetadata().getIntentName()), input);

        // Setto il response nel Json output finale
        output.setSpeech(response);
        output.setDisplayText(response);
    }

    private static String dispatcherFunction(int intent, AIResponse input) {
        /*
            Questa funzione è quella che discrimina qual è l'intento dell'utente e sceglie cosa
            fare e qual è la funzione adatta per la query al database.
         */

        String response = DefaultResponses.getOneRandomResponse();
        // Stringa per il response, verrà poi modificata col risultato della query specifica
        String oggetto; // Stringa col nome dell'oggetto

        System.out.println(TAG + intent);

        // Questo è lo switch per discriminare l'intendo dell'utente e agire di conseguenza
        switch (intent) {
            case Intent.WELCOME: // Welcome user
                adb.addUserIntent(Intent.WELCOME, null); // Per storico degli intenti ricevuti dall'utente

                response = DefaultResponses.getWelcomeResponse();
                break;

            case Intent.FUNCTIONING: // L'utente ha chiesto come si utilizza un particolare accessorio
                oggetto = MyTextUtils.cleanInput(input.getResult()
                        .getParameters()
                        .get(Constants.OBJECT)
                        .getAsString()
                );

                adb.addUserIntent(Intent.FUNCTIONING, oggetto); // Per storico degli intenti ricevuti dall'utente

                response = ResponseFactory
                        .getIstruzioniUsoOggettoResponse(
                                oggetto, // Oggetto
                                adb.getIstruzioniUsoAccessorio(oggetto), // Istruzioni d'uso
                                adb.doesExist(input.getResult()
                                        .getParameters()
                                        .get(Constants.OBJECT)
                                        .getAsString()
                                )
                        ); // Esistenza o no nel db
                break;

            case Intent.PRESENCE: // L'utente ha richiesto se è presente un certo oggetto (accessorio)
                oggetto = MyTextUtils.cleanInput(input.getResult()
                        .getParameters()
                        .get(Constants.OBJECT)
                        .getAsString());

                adb.addUserIntent(Intent.PRESENCE, oggetto); // Per storico degli intenti ricevuti dall'utente

                response = ResponseFactory.getPresenzaOggettoResponse(
                        oggetto, // Oggetto
                        adb.doesExist(input.getResult()
                                .getParameters()
                                .get(Constants.OBJECT)
                                .getAsString()
                        ) // Se è presente nel db
                );
                break;

            case Intent.ALL_ACESSORIES: // L'utente ha richiesto la lista degli accessori
                adb.addUserIntent(Intent.ALL_ACESSORIES, null); // Per storico degli intenti ricevuti dall'utente

                // Setto il response con la lista degli accessori nel Json output finale
                response = ResponseFactory.getListaAccessoriResponse(adb.getAllAccessori());
                break;

            case Intent.POSITION_ANSWER: // L'utente ha risposto riferendosi a una specifica parte dell'auto
                response = DefaultResponses.getOneRandomResponse();
                break;

            case Intent.AFFERMATIVE_ANSWER:
                Pair<Integer, String> entry = adb.getLastIntentInfo();
                if (entry.getKey() != 0) {
                    adb.addUserRequest(entry.getKey(), entry.getValue(), false);
                    response = "La tua richiesta è stata presa in considerazione...";
                } else
                    response = DefaultResponses.getOneRandomResponse();
                break;

            case Intent.NEGATIVE_ANSWER:
                break;

            case Intent.SIMPLE_OBJECT:
                break;

            default: // L'utente ha inserito qualcosa alla quale io non so rispondere
                break;
        }

        return response;
    }
}
