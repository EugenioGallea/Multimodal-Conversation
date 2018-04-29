package it.polito.s241876.database;

import it.polito.s241876.utils.Accessorio;
import it.polito.s241876.utils.Constants;
import it.polito.s241876.utils.MyTextUtils;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Questa classe è responsabile di gestire le query al database, ha diverse funzioni a seconda
 * della query richiesta.
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 */
public class AccessoriDB {
    private static AccessoriDB instance = null; // La faccio singleton
    private static Map<String, Integer> objectIdResolver = null; // Per fare prima a prendere gli id degli oggetti

    private AccessoriDB() {
        objectIdResolver = new HashMap<>();
        List<Accessorio> accessori = getAllAccessori();

        // Almeno ho in memoria la mappa degli oggetti con il loro id corrispondente --> faccio prima a prenderne uno singolo
        for (Accessorio accessorio : accessori)
            objectIdResolver.put(accessorio.getNome(), accessorio.getId());
    }

    public static AccessoriDB getInstance() {
        return (instance == null) ? new AccessoriDB() : instance;
    }

    public List<Accessorio> getAllAccessori() {
        /*
            Funzione che mi permette di ottenere la lista di tutti gli accessori che sono
            memorizzati nel mio database locale.
         */
        final String sql = "SELECT id, name, categoria, istruzioni_uso FROM accessori";

        List<Accessorio> accessori = new LinkedList<>();

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                Accessorio a = new Accessorio(rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("categoria"),
                        rs.getString("istruzioni_uso")
                );
                accessori.add(a);
            }

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accessori;
    }

    public String getIstruzioniUsoAccessorio(String oggetto) {
        /*
            Funzione che mi permette di ottenere le specifiche istruzioni d'uso per uno
            oggetto inserito dall'utente.
         */
        final String description = "istruzioni per utilizzo dell'oggetto " + oggetto;
        final String sql = "SELECT id, istruzioni_uso FROM accessori WHERE id=?";

        String result = "";
        int id = 0;
        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            if (objectIdResolver.containsKey(MyTextUtils.cleanInput(oggetto)))
                st.setInt(1, objectIdResolver.get(MyTextUtils.cleanInput(oggetto)));
            else {
                conn.close();
                return "";
            }
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                result += rs.getString(Constants.INSTRUCTIONS);
                id = rs.getInt(Constants.ID);
            }

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        insertUserRequest(Optional.of(id), description);

        return result;
    }

    public boolean doesExist(String oggetto) {
        /*
            Funzione che mi permette di verificare se un dato oggetto esiste oppure no
            nel mio database.
         */
        return objectIdResolver.containsKey(oggetto);
    }

    public int insertUserRequest(Optional<Integer> accessorio, String description) {
        final String sql = "INSERT INTO user_requests (id_accessorio, description, completed) VALUES (?, ?, ?)";

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            if (accessorio.isPresent())
                st.setInt(1, accessorio.get().intValue());
            st.setString(2, description);
            st.setInt(3, 0);
            st.execute();
            ResultSet set = st.getGeneratedKeys();

            while (set.next())
                return set.getInt(1);
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void addUserIntent(int id_intent, String oggetto) {
        final String sql = "INSERT INTO user_intents (id_intent, id_accessorio, description) VALUES (?, ?, ?)";

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, id_intent);
            st.setInt(2, Constants.IntentType.valueOf(id_intent).getIntentNo());
            st.setString(3, Constants.IntentType.valueOf(id_intent).getDescription());
            st.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void completeUserRequest(int id_request) {
        final String sql = "UPDATE user_requests SET completed = 1 WHERE id = ?";

        if (id_request == -1)
            return;

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, id_request);
            st.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Pair<Integer, String> getLastIntentInfo() {
        final String sql = "SELECT id_accessorio, description FROM user_intents ORDER BY id DESC LIMIT 1";

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            ResultSet rs = st.executeQuery();

            while (rs.next())
                return new Pair<>(rs.getInt("id_accessorio"), rs.getString("description"));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new Pair<>(0, "");
    }

    public void addUserRequest(Integer id_accessorio, String description, boolean completed) {

    }
}