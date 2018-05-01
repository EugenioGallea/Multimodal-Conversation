package it.polito.s241876.database;

import it.polito.s241876.utils.Accessorio;
import it.polito.s241876.utils.Action;
import it.polito.s241876.utils.Constants;
import it.polito.s241876.utils.MyTextUtils;
import javafx.util.Pair;

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
    private static int lastIntentRowId = 0;
    private static int lastIntentId = -1;
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

    public int getLastIntentId() {
        return lastIntentId;
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

    public void insertUserRequest(int oggetto, String description, int completed, int number) {
    }

    public void insertUserIntent(int id_intent, String oggetto) {
        insertUserIntent(id_intent, oggetto, -1);
    }

    public void insertUserIntent(int id_intent, String oggetto, int action) {
        final String sql = "INSERT INTO user_intents (id_intent, id_accessorio, description, id_action) VALUES (?, ?, ?, ?)";
        int id_accessorio = -1;

        if (objectIdResolver.containsKey(oggetto))
            id_accessorio = objectIdResolver.get(oggetto).intValue();

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, id_intent);
            st.setInt(2, id_accessorio);
            st.setString(3, Constants.IntentType.valueOf(id_intent).getDescription());
            st.setInt(4, action);
            st.execute();
            ResultSet rs = st.getGeneratedKeys();

            while (rs.next()) {
                lastIntentRowId = rs.getInt(1);
                lastIntentId = id_intent;
            }

            conn.close();

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
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        currentExecutingTask = 0;
    }

    public Pair<Integer, String> getLastIntentInfo() {
        final String sql = "SELECT id_accessorio, description FROM user_intents WHERE id = ?";
        Pair<Integer, String> result = new Pair<>(0, null);

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, lastIntentRowId);
            ResultSet rs = st.executeQuery();

            while (rs.next())
                result = new Pair<>(rs.getInt("id_accessorio"), rs.getString("description"));

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
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

    public boolean isTaskExecuting() {
        return (currentExecutingTask == 0) ? false : true;
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

    public boolean isObjectInMultiplePosition() {
        int id_accessorio = getAccessiorioOfRequest(lastUserRequestId);

        return isObjectInMultiplePosition(id_accessorio);
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

    public String getLastAskedObject() {
        final String sql = "SELECT name FROM accessori WHERE id = ?";
        final Pair<Integer, String> info = getLastIntentInfo();
        String result = "";

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, info.getKey().intValue());
            ResultSet rs = st.executeQuery();

            while (rs.next())
                result = rs.getString("name");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public int getLastObjectPosition() {
        Pair<Integer, String> info = getLastIntentInfo();
        int position = -1;

        final String sql = "SELECT posizione FROM accessori WHERE id = ?";

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, info.getKey().intValue());
            ResultSet rs = st.executeQuery();

            while (rs.next())
                position = rs.getInt("posizione");

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return position;
    }

    public int getLastIntentAction() {
        final String sql = "SELECT id_action FROM user_intents WHERE id = ?";
        int action = -1;

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, lastIntentRowId);
            ResultSet rs = st.executeQuery();

            while (rs.next())
                action = rs.getInt("id_action");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return action;
    }

    public String getIstruzioni(String oggetto, int action, String position) {
        int azione;
        String result = "";

        switch (action) {
            case Action.ACCENSIONE:
                azione = Action.ACCENSIONE;
                result = getIstruzioniAccensioneAccessorio(oggetto, Integer.parseInt(position));
                break;

            case Action.FUNZIONAMENTO_GENERALE:
                azione = Action.FUNZIONAMENTO_GENERALE;
                getIstruzioniUsoAccessorio(oggetto);
                break;

            default:
                break;
        }

        return result;
    }

    public int getObjectId(String oggetto) {
        return objectIdResolver.get(oggetto);
    }

    public void cleanUserIntentsTable() {
        final String sql = "DELETE FROM user_intents";

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.execute();

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}