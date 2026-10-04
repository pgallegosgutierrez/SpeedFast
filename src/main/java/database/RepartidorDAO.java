package database;

import model.Repartidor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RepartidorDAO {

    public static void guardar(Repartidor repartidor) {
        ConexionDB connector = new ConexionDB();
        String sql = "INSERT INTO repartidores (nombre) VALUES (?)";

        try (Connection conn = connector.conectar();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, repartidor.getNombre());
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar repartidor [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());
            throw new RuntimeException("No se pudo guardar el repartidor.\n" + e.getLocalizedMessage(), e);
        }
    }

    public static List<Repartidor> listarTodos() {
        ConexionDB connector = new ConexionDB();
        String sql = "SELECT id, nombre FROM repartidores ORDER BY id";
        List<Repartidor> repartidores = new ArrayList<>();

        try (Connection conn = connector.conectar();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("id"));
                String nombre = rs.getString("nombre");
                repartidores.add(new Repartidor(id, nombre));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar repartidores [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());
            throw new RuntimeException("No se pudieron cargar los repartidores.\n" + e.getLocalizedMessage(), e);
        }

        return repartidores;
    }
}