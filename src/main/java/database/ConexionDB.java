package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/speedfast_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "mysqlpass";

    /**
     * Abre una conexión nueva a speedfast_db. Quien la pide debe cerrarla
     * (los DAO lo hacen con try-with-resources).
     *
     * @return conexión abierta a la base de datos
     * @throws SQLException si no se puede conectar, con un mensaje claro del motivo
     */
    public static Connection conectar() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Error de conexión [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());

            String mensaje = "No se pudo conectar a MySQL.\n"
                    + "Revisa que el servidor esté encendido y que el usuario y la contraseña sean correctos.";
            if (e.getErrorCode() == 1049) {
                // Error 1049 = la base de datos no existe
                mensaje = "La base de datos speedfast_db no existe.\n"
                        + "Carga primero el script sql/speedfast_db.sql.";
            }
            throw new SQLException(mensaje, e.getSQLState(), e.getErrorCode(), e);
        }
    }
}
