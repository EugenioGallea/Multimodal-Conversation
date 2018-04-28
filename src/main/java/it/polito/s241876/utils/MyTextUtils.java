package it.polito.s241876.utils;

/**
 * Classe pensata per racchiudere tutte quelle funzioni che si occupano di manipolazione del
 * testo.
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 */

public class MyTextUtils {
    private static final String defaultRegex = "[-+.^:,?]";

    public static String cleanInput(String input) {
        return cleanInput(input, defaultRegex);
    }

    private static String cleanInput(String input, String regex) {
        return input.replaceAll(regex, "");
    }

}
