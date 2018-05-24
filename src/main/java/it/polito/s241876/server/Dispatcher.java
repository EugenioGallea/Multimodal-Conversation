package it.polito.s241876.server;

import ai.api.model.AIOutputContext;
import ai.api.model.AIResponse;
import ai.api.model.Fulfillment;
import com.google.gson.JsonElement;
import it.polito.s241876.database.AccessoriDB;
import it.polito.s241876.utils.*;

import java.util.List;

/**
 * Questa è la classe dove risiede un po' "l'intelligenza" dell'applicazione. Qui si determina cosa sta chiedendo
 * l'utente e cosa risponder lui, basandosi su quello che ci ha detto prima nella conversazione.
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 */
class Dispatcher {
    private static final String TAG = "[Dispatcher] ";
    private static final AccessoriDB adb = AccessoriDB.getInstance(); // Classe per interazione con il database

    static void dispatcherFunction(AIResponse input, Fulfillment output) {
        /*
            Questa funzione è quella che discrimina qual è l'intento dell'utente e sceglie cosa
            fare e qual è la funzione adatta per la query al database.
         */
        int intent = Integer.parseInt(input.getResult().getAction());
        String response = Response.getOneRandomResponse(); // Stringa per il response, verrà poi modificata col risultato della query specifica
        AIOutputContext ctx;
        JsonElement oggetto = input.getResult()
                .getParameters()
                .get(Constants.OBJECT); // Stringa col nome dell'oggetto

        // Questo è lo switch per discriminare l'intento dell'utente e agire di conseguenza
        switch (intent) {
            case Intent.ACCESSORI: // L'utente ha richiesto la lista degli accessori
                // Setto il response con la lista degli accessori nel Json output finale
                response = Response.getListaAccessoriResponse(adb.getAllAccessori());
                break;

            case Intent.PRESENZA: // L'utente ha richiesto se è presente un certo oggetto (accessorio)
                if (oggetto != null)
                    response = Response.getPresenzaOggettoResponse(
                            oggetto.getAsString(), // Oggetto
                            adb.doesExist(oggetto.getAsString()) // Se è presente nel db
                    );
                else
                    response = "Errore nella comprensione dell'oggetto";

                break;

            case Intent.UTILIZZO: // L'utente ha chiesto come si utilizza un particolare accessorio
                ctx = input.getResult().getContext(Context.UTILIZZO_ACCESSORIO);
                response = dispatcherFunctionAction(ctx);
                break;

            case Intent.POSIZIONE: // Qua ci finisco quando sono già nel contesto dell'utilizzo dell'oggetto, quindi so i dati da contesto
                AIOutputContext ctx_utilizzo = input.getResult().getContext(Context.UTILIZZO_ACCESSORIO);
                response = dispatcherFunctionAction(ctx_utilizzo);
                break;

            case Intent.RISPOSTA:
                int position;
                ctx = input.getResult().getContext(Context.UTILIZZO_ACCESSORIO);
                if (ctx.getParameters().get(Constants.POSITION) == null)
                    position = 0;
                else
                    position = Position.getValueGivenString(ctx.getParameters().get(Constants.POSITION).getAsString());

                adb.insertUserRequest(
                        adb.getObjectId(
                                ctx.getParameters().get(Constants.OBJECT).getAsString(),
                                position
                        ),
                        ctx.getParameters().get(Constants.ACTION).getAsString()
                );
                response = "La tua richiesta " + ctx.getParameters().get(Constants.RISPOSTA) + " è stata presa in considerazione";
                // TODO: dovrei fare partire quello che mi ha chiesto l'utente.
                break;

            case Intent.LISTA_POSIZIONI_OGGETTO:
                ctx = input.getResult().getContext(Context.UTILIZZO_ACCESSORIO);
                System.out.println(TAG + ctx.getName() + " " + ctx.getParameters().get(Constants.OBJECT));
                List<String> positions = adb.getListObjectPosition(ctx.getParameters().get(Constants.OBJECT));
                response = Response.getListObjectPositionsResponse(ctx.getParameters().get(Constants.OBJECT).getAsString(), positions);
                break;

            default: // L'utente ha inserito qualcosa alla quale io non so rispondere
                break;
        }

        output.setSpeech(response);
        output.setDisplayText(response);
    }

    private static String dispatcherFunctionAction(AIOutputContext context_utilizzo) {
        String response = "";
        JsonElement oggetto = context_utilizzo.getParameters().get(Constants.OBJECT);
        JsonElement azione = context_utilizzo.getParameters().get(Constants.ACTION);
        JsonElement posizione = context_utilizzo.getParameters().get(Constants.POSITION);

        if (azione.isJsonNull()) return "dispatcherFunctionAction: il campo azione non può essere vuoto!";
        if (oggetto.isJsonNull()) return "dispatcherFunctionAction: il campo oggetto non può essere vuoto!";

        switch (MyTextUtils.cleanInput(azione.getAsString())) {
            case Action.ACCENSIONE: // Accensione
                if ((posizione == null || posizione.isJsonNull()) && adb.isPositionNeeded(oggetto.getAsString()))
                    return Response.getOneRandomPositionResponse(oggetto.getAsString());

                if (!adb.isPositionNeeded(oggetto.getAsString())) { // Non serve sapere la posizione per l'oggeto
                    String istr = adb.getIstruzioniAccensioneAccessorio(
                            oggetto.getAsString()
                    );
                    response = Response.getAccensioneAccessorioResponse(oggetto.getAsString(), istr, true);
                    break;
                }

                // Se arrivo qui è perché ho la posizione e devo trovare il response
                assert posizione != null;
                int position = (posizione.isJsonNull()) ? -1 : Position.getValueGivenString(posizione.getAsString());
                if (position != -1) {
                    String istr = adb.getIstruzioniAccensioneAccessorio(
                            oggetto.getAsString(),
                            position
                    );
                    response = Response.getAccensioneAccessorioResponse(oggetto.getAsString(), istr, true);
                } else
                    response = Response.getOneRandomPositionResponse(oggetto.getAsString());
                break;

            case Action.FUNZIONAMENTO_GENERALE: // Funzionamento generale
                response = Response
                        .getIstruzioniUsoOggettoResponse(
                                MyTextUtils.cleanInput(oggetto.getAsString()), // Oggetto
                                adb.getIstruzioniUsoAccessorio(MyTextUtils.cleanInput(oggetto.getAsString())), // Istruzioni d'uso
                                adb.doesExist(MyTextUtils.cleanInput(oggetto.getAsString()))); // Esistenza o no nel db
                context_utilizzo.setLifespan(0);
                break;
        }

        return response;
    }
}
