package it.polito.s241876.database;

import it.polito.s241876.utils.Accessorio;
import it.polito.s241876.utils.Constants;
import it.polito.s241876.utils.MyTextUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Questa classe è responsabile di gestire le query al database, ha diverse funzioni a seconda
 * della query richiesta.
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 */
public class AccessoriDB {
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
                Accessorio a = new Accessorio(rs.getString("id"), rs.getString("name"), rs.getString("categoria"), rs.getString("istruzioni_uso"));
                accessori.add(a);
            }

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accessori;
    }

    public String getIstruzioniUso(String oggetto){
        /*
            Funzione che mi permette di ottenere le specifiche istruzioni d'uso per uno
            oggetto inserito dall'utente.
         */
        final String description = "istruzioni per utilizzo dell'oggetto " + oggetto;
        final String sql = "SELECT id, istruzioni_uso FROM accessori WHERE name=?";
        String result = "";
        int id = 0;
        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setString(1, MyTextUtils.cleanInput(oggetto));
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
        final String sql = "SELECT COUNT(DISTINCT id) AS 'tot' FROM accessori WHERE name=?";
        int count = 0;
        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setString(1, MyTextUtils.cleanInput(oggetto));
            ResultSet rs = st.executeQuery();

            while (rs.next())
                count = rs.getInt("tot");

            conn.close();

            if (count != 0)
                return true;
            else
                return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void insertUserRequest(Optional<Integer> accessorio, String description) {
        final String sql = "INSERT INTO user_requests (id_accessorio, description) VALUES (?, ?)";
        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, accessorio.get().intValue());
            st.setString(2, description);
            st.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}