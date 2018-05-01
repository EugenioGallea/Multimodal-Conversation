package it.polito.s241876.server;

import ai.api.model.AIResponse;
import it.polito.s241876.database.AccessoriDB;
import it.polito.s241876.utils.*;
import javafx.util.Pair;

/**
 * Questa è la classe dove risiede un po' "l'intelligenza" dell'applicazione. Qui si determina cosa sta chiedendo
 * l'utente e cosa risponder lui, basandosi su quello che ci ha detto prima nella conversazione.
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 */
public class Dispatcher {
    private static final String TAG = "[Dispatcher] ";
    private static final AccessoriDB adb = AccessoriDB.getInstance(); // Classe per interazione con il database

    public static String dispatcherFunction(int intent, AIResponse input) {
        /*
            Questa funzione è quella che discrimina qual è l'intento dell'utente e sceglie cosa
            fare e qual è la funzione adatta per la query al database.
         */
        String response = Response.getOneRandomResponse(); // Stringa per il response, verrà poi modificata col risultato della query specifica
        String oggetto; // Stringa col nome dell'oggetto

        // Questo è lo switch per discriminare l'intendo dell'utente e agire di conseguenza
        switch (intent) {
            case Intent.BENVENUTO: // Welcome user
                response = Response.getWelcomeResponse();
                adb.insertUserIntent(Intent.BENVENUTO, null); // Per storico degli intenti ricevuti dall'utente
                break;

            case Intent.OGGETTO:
                oggetto = MyTextUtils.cleanInput(input.getResult()
                        .getParameters()
                        .get(Constants.OBJECT)
                        .getAsString());

                response = Response.getOneAboutObjectResponse(oggetto);
                adb.insertUserIntent(Intent.OGGETTO, oggetto); // Per storico degli intenti ricevuti dall'utente
                break;

            case Intent.ACCESSORI: // L'utente ha richiesto la lista degli accessori
                // Setto il response con la lista degli accessori nel Json output finale
                response = Response.getListaAccessoriResponse(adb.getAllAccessori());
                adb.insertUserIntent(Intent.ACCESSORI, null); // Per storico degli intenti ricevuti dall'utente
                break;

            case Intent.PRESENZA: // L'utente ha richiesto se è presente un certo oggetto (accessorio)
                oggetto = MyTextUtils.cleanInput(input.getResult()
                        .getParameters()
                        .get(Constants.OBJECT)
                        .getAsString());

                response = Response.getPresenzaOggettoResponse(
                        oggetto, // Oggetto
                        adb.doesExist(oggetto) // Se è presente nel db
                );

                adb.insertUserIntent(Intent.PRESENZA, oggetto); // Per storico degli intenti ricevuti dall'utente
                break;

            case Intent.UTILIZZO: // L'utente ha chiesto come si utilizza un particolare accessorio
                oggetto = MyTextUtils.cleanInput(input.getResult()
                        .getParameters()
                        .get(Constants.OBJECT)
                        .getAsString()
                );

                String azione = input.getResult()
                        .getParameters()
                        .get(Constants.ACTION)
                        .getAsString();

                response = dispatcherFunctionAction(oggetto, azione);
                break;

            case Intent.POSIZIONE: // L'utente ha risposto riferendosi a una specifica parte dell'auto
                oggetto = adb.getLastAskedObject();
                int azione_p = adb.getLastIntentAction();
                if (oggetto != null) {
                    String position = input.getResult().getParameters().get("posizione").getAsString();
                    String number = input.getResult().getParameters().get("numero").getAsString();

                    if (number.equals("")) number = "-1";
                    if (position.equals("")) position = "-1";
                    if (number.equals("2")) position = "1";

                    if (MyTextUtils.isNumeric(position) && MyTextUtils.isNumeric(number)) { // Ok, so a cosa si riferisce, aggiungo la request come non completata ancora.
                        response = adb.getIstruzioni(oggetto, azione_p, position);

                        if (response.equals("") && !number.equals("2")) return Response.getOneRandomResponse();

                        response += Response.getOneRandomOfferResponse();
                    } else
                        response = "Posizione e quantità errati.";
                } else
                    response = "Non mi hai chiesto di nessun oggetto prima d'ora...";

                adb.insertUserIntent(Intent.POSIZIONE, oggetto, azione_p); // Per storico degli intenti ricevuti dall'utente
                break;

            case Intent.RISPOSTA_POSITIVA:
                Pair<Integer, String> entry = adb.getLastIntentInfo(); // id accessorio - description
                if (entry.getKey() != 0) {
                    // TODO: verrà lanciata una task per soddisfare la richiesta dell'utente per lo specifico accessorio se ce n'era una già prima
                    elaboratePositiveRequest(entry);
                    response = "La tua richiesta è stata presa in considerazione...";
                    adb.insertUserIntent(Intent.RISPOSTA_POSITIVA, adb.getLastAskedObject()); // Per storico degli intenti ricevuti dall'utente // TODO: mettere l'oggetto relativo
                    adb.cleanUserIntentsTable();
                }
                break;

            case Intent.RISPOSTA_NEGATIVA:
                oggetto = "";
                if (adb.isTaskExecuting() && adb.getLastIntentId() != Intent.RISPOSTA_POSITIVA) {
                    adb.deleteLastUserRequest();
                    oggetto = adb.getLastAskedObject();
                    response = Response.getDismissResponse();
                } else
                    response = Response.getNoTaskExecutingResponse();

                adb.insertUserIntent(Intent.RISPOSTA_NEGATIVA, oggetto); // Per storico degli intenti ricevuti dall'utente
                break;

            case Intent.ABBANDONA:
                oggetto = "";
                if (adb.isTaskExecuting()) {
                    adb.deleteLastUserRequest();
                    oggetto = adb.getLastAskedObject();
                    response = Response.getDismissResponse();
                } else
                    response = Response.getNoTaskExecutingResponse();

                adb.insertUserIntent(Intent.RISPOSTA_NEGATIVA, oggetto); // Per storico degli intenti ricevuti dall'utente
                break;

            default: // L'utente ha inserito qualcosa alla quale io non so rispondere
                adb.insertUserIntent(-1, null); // Per storico degli intenti ricevuti dall'utente
                break;
        }

        return response;
    }

    private static String dispatcherFunctionAction(String oggetto, String az) {
        String response = "";
        String azione = MyTextUtils.cleanInput(az);
        String lastOggetto;
        if (!MyTextUtils.isNumeric(azione))
            return Response.getErrorActionInterpretation();

        if (oggetto.equals(""))
            lastOggetto = adb.getLastAskedObject();
        else
            lastOggetto = MyTextUtils.cleanInput(oggetto);

        switch (Integer.parseInt(azione)) {
            case Action.ACCENSIONE: // Accensione
                int posizione = adb.getLastObjectPosition();

                System.out.println(TAG + posizione + " " + lastOggetto);

                if (posizione == -1 && lastOggetto == null) {
                    return "Non so a che oggetto tu ti stia riferendo...";
                }

                if (posizione == -1 && adb.isObjectInMultiplePosition(adb.getObjectId(lastOggetto)))
                    return Response.getAccensioneAccessorioResponse(lastOggetto, "", false);

                String istr = adb.getIstruzioniAccensioneAccessorio(lastOggetto, posizione);

                response = Response
                        .getAccensioneAccessorioResponse(lastOggetto, istr, posizione != -1);

                adb.insertUserIntent(Intent.UTILIZZO, lastOggetto, Action.ACCENSIONE); // Per storico degli intenti ricevuti dall'utente
                break;

            case Action.FUNZIONAMENTO_GENERALE: // Funzionamento generale
                response = Response
                        .getIstruzioniUsoOggettoResponse(
                                lastOggetto, // Oggetto
                                adb.getIstruzioniUsoAccessorio(lastOggetto), // Istruzioni d'uso
                                adb.doesExist(lastOggetto)); // Esistenza o no nel db
                adb.insertUserIntent(Intent.UTILIZZO, lastOggetto, Action.FUNZIONAMENTO_GENERALE); // Per storico degli intenti ricevuti dall'utente
                break;
        }

        return response;
    }

    private static void elaboratePositiveRequest(Pair<Integer, String> entry) {
    }
}
