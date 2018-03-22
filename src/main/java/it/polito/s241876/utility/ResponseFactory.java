package it.polito.s241876.utility;

import java.util.List;

public class ResponseFactory {
    public static String getListaAccessoriResponse(List<Accessorio> a_list){
        String response = "Eccoti l'elenco di cosa è presente in macchina: ";
        int i = 0;
        for(Accessorio a : a_list) {
            response += a.getNome();
            i++;
            if( i != a_list.size() )
                response += ", ";
        }

        return response;
    }

    public static String getIstruzioniUsoOggettoResponse(String oggetto, String istr, boolean exists){
        String response = "";
        if(exists)
            response = "Le istruzioni d'uso per l'accessorio \"" + oggetto + "\" sono: " + istr;
        else
            response += "Non conosco il funzionamento dell'oggetto \""
                        + oggetto + "\"";

        return response;
    }

    public static String getPresenzaOggettoResponse(String oggetto, boolean exists){
        String response = "";
        if(exists)
            response += "L'oggetto \"" + oggetto + "\" è presente su questo veicolo.";
        else
            response += "L'oggetto \"" + oggetto + "\" non è presente su questo veicolo.";

        return response;
    }
}
