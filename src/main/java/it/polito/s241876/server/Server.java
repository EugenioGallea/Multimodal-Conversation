package it.polito.s241876.server;

import ai.api.GsonFactory;
import ai.api.model.AIResponse;
import ai.api.model.Fulfillment;
import com.google.gson.Gson;
import it.polito.s241876.utils.Constants;

import static spark.Spark.post;

/**
 * Questa è la classe che riceverà le richieste da parte di DialogFlow contenenti ciò che l'assistente
 * conversazionale è riuscito a interpretare.
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 */

public class Server {
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
        Dispatcher.dispatcherFunction(input, output);
    }
}
