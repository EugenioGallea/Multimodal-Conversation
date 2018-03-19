package it.polito.s241876.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe che segue singleton pattern per stabilire una connessione con il database.
 * Creata una volta, rimane comune a tutte altre istanze del webhook evocate.
 *
 * @author <a href="mailto:s241876@studenti.polito.it">Eugenio Gallea</a>
 * @author <a href="mailto:luigi.derussis@uniupo.it">Luigi De Russis</a>
 */
public class DBConnect {
    static private final String dbLoc = "jdbc:sqlite:src/main/resources/accessoriDB";
    static private DBConnect instance = null;

    private DBConnect() {
        instance = this;
    }

    public static DBConnect getInstance() {
        if (instance == null)
            return new DBConnect();
        else {
            return instance;
        }
    }

    public Connection getConnection() throws SQLException {
        try {

            Connection conn = DriverManager.getConnection(dbLoc);
            return conn;

        } catch (SQLException e) {
            throw new SQLException("Non riesco a connettermi a " + dbLoc, e);
        }
    }

}