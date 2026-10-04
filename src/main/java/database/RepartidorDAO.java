package database;

import model.Repartidor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Operaciones CRUD sobre la tabla repartidores.
 */
public class RepartidorDAO {

    /**
     * Inserta un repartidor nuevo.
     *
     * @param repartidor repartidor a guardar (todavía sin id)
     * @return el id que le asignó la base de datos
     */
    public static int create(Repartidor repartidor) {
        ConexionDB connector = new ConexionDB();
        String sql = "INSERT INTO repartidores (nombre) VALUES (?)";

        try (Connection conn = connector.conectar();
             PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, repartidor.getNombre());
            statement.executeUpdate();

            return UtilDAO.idGenerado(statement);

        } catch (SQLException e) {
            System.out.println("Error al guardar repartidor [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());
            throw new RuntimeException("No se pudo guardar el repartidor.\n" + UtilDAO.mensajeError(e), e);
        }
    }

    /**
     * Lee todos los repartidores, ordenados por id.
     *
     * @return lista de repartidores (vacía si no hay ninguno)
     */
    public static List<Repartidor> readAll() {
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
            throw new RuntimeException("No se pudieron cargar los repartidores.\n" + UtilDAO.mensajeError(e), e);
        }

        return repartidores;
    }

    /**
     * Actualiza el nombre de un repartidor que ya existe (se busca por su id).
     *
     * @param repartidor repartidor con su id y el nombre nuevo
     */
    public static void update(Repartidor repartidor) {
        ConexionDB connector = new ConexionDB();
        String sql = "UPDATE repartidores SET nombre = ? WHERE id = ?";

        try (Connection conn = connector.conectar();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, repartidor.getNombre());
            statement.setInt(2, Integer.parseInt(repartidor.getId()));

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("El repartidor " + repartidor.getId() + " ya no existe en la base de datos.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Id de repartidor inválido: " + e.getLocalizedMessage());
            throw new RuntimeException("El ID del repartidor debe ser un número entero.", e);
        } catch (SQLException e) {
            System.out.println("Error al actualizar repartidor [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());
            throw new RuntimeException("No se pudo actualizar el repartidor.\n" + UtilDAO.mensajeError(e), e);
        }
    }

    /**
     * Elimina un repartidor. Si tiene entregas registradas, la base de datos no lo permite.
     *
     * @param id identificador del repartidor a eliminar
     */
    public static void delete(String id) {
        ConexionDB connector = new ConexionDB();
        String sql = "DELETE FROM repartidores WHERE id = ?";

        try (Connection conn = connector.conectar();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, Integer.parseInt(id));

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("El repartidor " + id + " ya no existe en la base de datos.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Id de repartidor inválido: " + e.getLocalizedMessage());
            throw new RuntimeException("El ID del repartidor debe ser un número entero.", e);
        } catch (SQLException e) {
            System.out.println("Error al eliminar repartidor [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());
            throw new RuntimeException("No se pudo eliminar el repartidor.\n" + UtilDAO.mensajeError(e), e);
        }
    }
}
