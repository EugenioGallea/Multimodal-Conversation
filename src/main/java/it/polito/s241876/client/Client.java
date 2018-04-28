package it.polito.s241876.client;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import it.polito.s241876.utils.Constants;

import java.util.Scanner;

/**
 * Thread per le richieste dell'utente. Poi si evolverà nell'uso dell'input vocale.
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 */
public class Client extends Thread {
    private static final String TAG = "[Client] ";
    private static final String INPUT_PROMPT = "> ";
    private static final int ERROR_EXIT_CODE = 1;

    public static void entryPoint() {
        AIConfiguration configuration = new AIConfiguration(Constants.API_KEY);
        AIDataService dataService = new AIDataService(configuration);
        AIRequest request;
        AIResponse response = null;
        String input = "";
        Scanner reader = new Scanner(System.in);

        while (!input.equals("esci")) {
            System.out.println(INPUT_PROMPT + TAG + "Scrivi qualcosa: ");
            input = reader.nextLine();
            request = new AIRequest(input);
            try {
                response = dataService.request(request);
            } catch (AIServiceException e) {
                e.printStackTrace();
            }

            assert response != null;

            if (response.getStatus().getCode() == 200) {
                System.out.println(INPUT_PROMPT + TAG + response.getResult().getFulfillment().getSpeech());
            } else {
                System.err.println(INPUT_PROMPT + TAG + response.getStatus().getErrorDetails());
            }
        }

        System.out.println(TAG + "Ciao ciao!");
        reader.close();
    }
}
