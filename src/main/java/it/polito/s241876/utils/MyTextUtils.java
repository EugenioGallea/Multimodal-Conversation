package it.polito.s241876.utils;

/**
 * Classe pensata per racchiudere tutte quelle funzioni che si occupano di manipolazione del
 * testo.
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 */

public class MyTextUtils {
    private static final String defaultRegex = "[-+.^:,]\"\'";

    public static String cleanInput(String input) {
        return cleanInput(input, defaultRegex);
    }

    private static String cleanInput(String input, String regex) {
        String in = input.replaceAll(regex, "");
        int index = in.indexOf('?');
        String result = index == -1 ? in : in.substring(0, index);
        return convertToLowerCase(result, 0, result.length());
    }

    private static String convertToLowerCase(String s, int begin, int end) {
        if (s.length() == 0) return "";

        return s.substring(begin, end).toLowerCase();
    }

    public static boolean isNumeric(String azione) {
        try {
            int i = Integer.parseInt(azione);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
