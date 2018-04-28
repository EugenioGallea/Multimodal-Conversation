package it.polito.s241876.main;

import it.polito.s241876.client.Client;
import it.polito.s241876.server.Server;

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
    public static final Thread serverThread = new Thread(() -> Server.entryPoint()); // Thread per elaborare le informazioni interagendo con il DB
    public static final Thread clientThread = new Thread(() -> Client.entryPoint()); // Thread per interagire con l'utente

    public static void main(String[] args) {
        serverThread.start();
        clientThread.start();

        try {
            serverThread.join();
            clientThread.join();
        } catch (InterruptedException e) {
            // Niente, qualcosa è crashato
        }
    }
}
