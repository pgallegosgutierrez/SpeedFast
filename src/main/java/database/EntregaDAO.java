package database;

import model.Entrega;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;

public class EntregaDAO {

    public static void guardar(Entrega entrega) {
        ConexionDB connector = new ConexionDB();
        String sql = "INSERT INTO entrega (id_pedido, id_repartidor, fecha, hora) VALUES (?,?,?,?)";

        try (Connection conn = connector.conectar();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, Integer.parseInt(entrega.getIdPedido()));
            statement.setInt(2, Integer.parseInt(entrega.getIdRepartidor()));
            statement.setDate(3, Date.valueOf(entrega.getFecha()));   // LocalDate -> java.sql.Date
            statement.setTime(4, Time.valueOf(entrega.getHora()));    // LocalTime -> java.sql.Time

            statement.executeUpdate();

        } catch (NumberFormatException e) {
            System.out.println("Id de pedido o repartidor inválido: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        } catch (SQLException e) {
            // Error 1452 = la FK no existe (pedido o repartidor no registrado)
            System.out.println("Error al guardar entrega [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }
}
