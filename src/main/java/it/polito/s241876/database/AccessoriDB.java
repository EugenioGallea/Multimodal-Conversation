package it.polito.s241876.database;

import com.google.gson.JsonElement;
import it.polito.s241876.utils.Accessorio;
import it.polito.s241876.utils.Constants;
import it.polito.s241876.utils.MyTextUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Questa classe è responsabile di gestire le query al database, ha diverse funzioni a seconda
 * della query richiesta.
 *
 * Posizione:
 *      0 = può stare solo in un posto
 *      1 = anteriore
 *      2 = posteriore
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 */
public class AccessoriDB {
    private static final String TAG = "[AccessoriDB] ";
    private static AccessoriDB instance = null; // La faccio singleton
    private static HashMap<String, Integer> objectIdResolver = new HashMap<>();
    ; // Per fare prima a prendere gli id degli oggetti
    private static int lastUserRequestId = 0;
    private static int currentExecutingTask = 0; // Indice che corrisponde alla richiesta dell'utente che è presa in carico

    private AccessoriDB() {
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

        insertUserRequest(id, description);

        return result;
    }

    public boolean doesExist(String oggetto) {
        /*
            Funzione che mi permette di verificare se un dato oggetto esiste oppure no
            nel mio database.
         */
        return objectIdResolver.containsKey(oggetto);
    }

    public int insertUserRequest(int accessorio, String description) {
        return insertUserRequest(accessorio, description, 0);
    }

    public int insertUserRequest(int accessorio, String description, int completed) {
        /*
            0 = non completata
            1 = completata
         */
        final String sql = "INSERT INTO user_requests (id_accessorio, description, completed) VALUES (?, ?, ?)";

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, accessorio);
            st.setString(2, description);
            st.setInt(3, completed);
            st.execute();
            ResultSet set = st.getGeneratedKeys();

            while (set.next()) {
                lastUserRequestId = set.getInt(1);
                if (currentExecutingTask == 0)
                    currentExecutingTask = lastUserRequestId;

                return lastUserRequestId;
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
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
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        currentExecutingTask = 0;
    }

    public void deleteLastUserRequest() {
        final String sql = "DELETE FROM user_requests WHERE id = ?";

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, lastUserRequestId);
            st.execute();
            conn.close();

            lastUserRequestId = 0;
            currentExecutingTask = 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public String getIstruzioniAccensioneAccessorio(String oggetto) {
        return getIstruzioniAccensioneAccessorio(oggetto, -1);
    }

    public String getIstruzioniAccensioneAccessorio(String oggetto, int posizione) {
        String sql;
        if (posizione == -1)
            sql = "SELECT accensione FROM accessori WHERE name = ?";
        else
            sql = "SELECT accensione FROM accessori WHERE name = ? AND posizione = ?";

        String result = "";
        String cleanOggetto = MyTextUtils.cleanInput(oggetto);

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setString(1, cleanOggetto);
            if (posizione != -1)
                st.setInt(2, posizione);
            ResultSet rs = st.executeQuery();

            while (rs.next())
                result = rs.getString("accensione");

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean isObjectInMultiplePosition(int id_accessorio) {
        return (howManyOccurrencies(id_accessorio) == 0) ? true : false;
    }

    private int howManyOccurrencies(int id_accessorio) {
        final String sql = "SELECT COUNT(*) AS occurrencies FROM accessori WHERE id = ?";
        int count = -1;

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, id_accessorio);
            ResultSet rs = st.executeQuery();

            while (rs.next())
                count = rs.getInt("occurrencies");

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    private int getAccessiorioOfRequest(int lastUserRequestId) {
        final String sql = "SELECT id_accessorio FROM user_requests WHERE id = ?";
        int id = -1;

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, lastUserRequestId);
            ResultSet rs = st.executeQuery();

            while (rs.next())
                id = rs.getInt(Constants.IDACCESSORIO);

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return id;
    }

    public int getObjectId(String oggetto, int position) {
        final String sql = "SELECT id FROM accessori WHERE name = ? AND posizione = ?";
        int id = 1;

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setString(1, oggetto);
            st.setInt(2, position);
            ResultSet rs = st.executeQuery();

            while (rs.next())
                id = rs.getInt("id");

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return id;
    }

    public boolean isPositionNeeded(String oggetto) {
        final String sql = "SELECT COUNT(*) as tot FROM accessori WHERE name = ?";
        int count = 1;

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setString(1, oggetto);
            ResultSet rs = st.executeQuery();

            while (rs.next())
                count = rs.getInt("tot");

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count != 1;
    }

    public List<String> getListObjectPosition(JsonElement oggetto) {
        final String sql = "SELECT posizione FROM accessori WHERE name = ?";
        List<String> positions = new LinkedList<>();
        if (oggetto == null)
            return null;

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setString(1, oggetto.getAsString());
            ResultSet rs = st.executeQuery();

            while (rs.next())
                positions.add(rs.getString("posizione"));

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return positions;
    }
}