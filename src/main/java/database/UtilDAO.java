package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Código común de los DAO: lectura del id que genera MySQL al insertar
 * y traducción de los errores más frecuentes a mensajes claros.
 */
public class UtilDAO {

    private UtilDAO() {
        // Clase utilitaria: no se crean instancias
    }

    /**
     * Devuelve el id (AUTO_INCREMENT) de la fila recién insertada. La sentencia debe
     * haberse preparado con Statement.RETURN_GENERATED_KEYS.
     *
     * @param statement sentencia INSERT que ya se ejecutó
     * @return el id generado por la base de datos
     * @throws SQLException si la base de datos no devuelve el id
     */
    public static int idGenerado(PreparedStatement statement) throws SQLException {
        try (ResultSet claves = statement.getGeneratedKeys()) {
            claves.next();
            return claves.getInt(1);
        }
    }

    /**
     * Traduce los errores más comunes de MySQL a un mensaje que el usuario pueda entender.
     *
     * @param e error devuelto por la base de datos
     * @return mensaje en español para mostrar en pantalla
     */
    public static String mensajeError(SQLException e) {
        switch (e.getErrorCode()) {
            case 1146:
                // Error 1146 = la tabla no existe
                return "Faltan las tablas en speedfast_db. Carga primero el script sql/speedfast_db.sql.";
            case 1406:
                // Error 1406 = el texto no cabe en la columna
                return "Uno de los textos es más largo de lo que permite la base de datos.";
            case 1451:
                // Error 1451 = hay filas de otra tabla (entregas) que apuntan a esta
                return "Tiene entregas registradas. Elimina primero esas entregas.";
            case 1452:
                // Error 1452 = la FK no existe (pedido o repartidor no registrado)
                return "El pedido o el repartidor indicado no existe en la base de datos (error 1452 de MySQL).";
            default:
                return e.getLocalizedMessage();
        }
    }
}
