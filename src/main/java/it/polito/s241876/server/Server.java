package it.polito.s241876.server;

import ai.api.GsonFactory;
import ai.api.model.AIResponse;
import ai.api.model.Fulfillment;
import com.google.gson.Gson;
import it.polito.s241876.database.AccessoriDB;
import it.polito.s241876.utils.*;

import static spark.Spark.post;

/**
 * Questa è la classe che riceverà le richieste da parte di DialogFlow contenenti ciò che l'assistente
 * conversazionale è riuscito a interpretare
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 */

public class Server {
    private static final String TAG = "[Server] ";

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

        String response = ""; // Stringa per il response, verrà poi modificata col risultato della query specifica
        AccessoriDB adb = new AccessoriDB(); // Classe per le interazioni con il DB

        // Questo è lo switch per discriminare l'intendo dell'utente e agire di conseguenza

        switch (intent) {
            case Intent.WELCOME: // Welcome user
                response = DefaultResponses.getWelcomeResponse();
                break;

            case Intent.FUNCTIONING: // L'utente ha chiesto come si utilizza un particolare accessorio
                String oggetto2 = MyTextUtils.cleanInput(input.getResult()
                        .getParameters()
                        .get(Constants.OBJECT)
                        .getAsString()
                );

                response = ResponseFactory
                        .getIstruzioniUsoOggettoResponse(
                                oggetto2, // Oggetto
                                adb.getIstruzioniUso(oggetto2), // Istruzioni d'uso
                                adb.doesExist(input.getResult()
                                        .getParameters()
                                        .get(Constants.OBJECT)
                                        .getAsString()
                                )
                        ); // Esistenza o no nel db
                break;

            case Intent.PRESENCE: // L'utente ha richiesto se è presente un certo oggetto (accessorio)
                response = ResponseFactory.getPresenzaOggettoResponse(
                        MyTextUtils.cleanInput(input.getResult()
                                .getParameters()
                                .get(Constants.OBJECT)
                                .getAsString()
                        ), // Oggetto
                        adb.doesExist(input.getResult()
                                .getParameters()
                                .get(Constants.OBJECT)
                                .getAsString()
                        ) // Se è presente nel db
                );
                break;

            case Intent.ALL_ACESSORIES: // L'utente ha richiesto la lista degli accessori
                // Setto il response con la lista degli accessori nel Json output finale
                response = ResponseFactory.getListaAccessoriResponse(adb.getAllAccessori());

                break;

            case Intent.POSITION_ANSWER: // L'utente ha risposto riferendosi a una specifica parte dell'auto
                break;

            case Intent.AFFERMATIVE_ANSWER:
                break;

            case Intent.NEGATIVE_ANSWER:
                break;

            default: // L'utente ha inserito qualcosa alla quale io non so rispondere
                response = DefaultResponses.getOneRandomResponse(); // Prendo un response di default a caso

                break;
        }

        return response;
    }
}
