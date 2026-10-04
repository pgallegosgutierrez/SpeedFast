package database;

import model.Entrega;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Operaciones CRUD sobre la tabla entregas.
 */
public class EntregaDAO {

    /**
     * Inserta una entrega nueva, asociada a un pedido y a un repartidor.
     *
     * @param entrega entrega a guardar (todavía sin id)
     * @return el id que le asignó la base de datos
     */
    public static int create(Entrega entrega) {
        ConexionDB connector = new ConexionDB();
        String sql = "INSERT INTO entregas (id_pedido, id_repartidor, fecha, hora) VALUES (?,?,?,?)";

        try (Connection conn = connector.conectar();
             PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            cargarDatos(statement, entrega);
            statement.executeUpdate();

            return UtilDAO.idGenerado(statement);

        } catch (NumberFormatException e) {
            System.out.println("Id de pedido o repartidor inválido: " + e.getLocalizedMessage());
            throw new RuntimeException("El ID del pedido y el del repartidor deben ser números enteros.", e);
        } catch (SQLException e) {
            System.out.println("Error al guardar entrega [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());
            throw new RuntimeException("No se pudo guardar la entrega.\n" + UtilDAO.mensajeError(e), e);
        }
    }

    /**
     * Lee todas las entregas, ordenadas por id.
     *
     * @return lista de entregas (vacía si no hay ninguna)
     */
    public static List<Entrega> readAll() {
        ConexionDB connector = new ConexionDB();
        String sql = "SELECT id, id_pedido, id_repartidor, fecha, hora FROM entregas ORDER BY id";
        List<Entrega> entregas = new ArrayList<>();

        try (Connection conn = connector.conectar();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("id"));
                String idPedido = rs.getString("id_pedido");
                String idRepartidor = rs.getString("id_repartidor");
                Date fecha = rs.getDate("fecha");
                Time hora = rs.getTime("hora");

                // La tabla permite fecha y hora vacías: en ese caso quedan en null
                entregas.add(new Entrega(id, idPedido, idRepartidor,
                        fecha == null ? null : fecha.toLocalDate(),      // java.sql.Date -> LocalDate
                        hora == null ? null : hora.toLocalTime()));      // java.sql.Time -> LocalTime
            }

        } catch (SQLException e) {
            System.out.println("Error al listar entregas [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());
            throw new RuntimeException("No se pudieron cargar las entregas.\n" + UtilDAO.mensajeError(e), e);
        }

        return entregas;
    }

    /**
     * Actualiza el pedido, el repartidor, la fecha y la hora de una entrega que ya existe (se busca por su id).
     *
     * @param entrega entrega con su id y los datos nuevos
     */
    public static void update(Entrega entrega) {
        ConexionDB connector = new ConexionDB();
        String sql = "UPDATE entregas SET id_pedido = ?, id_repartidor = ?, fecha = ?, hora = ? WHERE id = ?";

        try (Connection conn = connector.conectar();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            cargarDatos(statement, entrega);
            statement.setInt(5, Integer.parseInt(entrega.getId()));

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("La entrega " + entrega.getId() + " ya no existe en la base de datos.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Id de entrega, pedido o repartidor inválido: " + e.getLocalizedMessage());
            throw new RuntimeException("El ID de la entrega, del pedido y del repartidor deben ser números enteros.", e);
        } catch (SQLException e) {
            System.out.println("Error al actualizar entrega [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());
            throw new RuntimeException("No se pudo actualizar la entrega.\n" + UtilDAO.mensajeError(e), e);
        }
    }

    /**
     * Elimina una entrega.
     *
     * @param id identificador de la entrega a eliminar
     */
    public static void delete(String id) {
        ConexionDB connector = new ConexionDB();
        String sql = "DELETE FROM entregas WHERE id = ?";

        try (Connection conn = connector.conectar();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, Integer.parseInt(id));

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("La entrega " + id + " ya no existe en la base de datos.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Id de entrega inválido: " + e.getLocalizedMessage());
            throw new RuntimeException("El ID de la entrega debe ser un número entero.", e);
        } catch (SQLException e) {
            System.out.println("Error al eliminar entrega [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());
            throw new RuntimeException("No se pudo eliminar la entrega.\n" + UtilDAO.mensajeError(e), e);
        }
    }

    /**
     * Pone en la sentencia los cuatro datos de una entrega, en el mismo orden que usan
     * el INSERT y el UPDATE: pedido, repartidor, fecha y hora.
     */
    private static void cargarDatos(PreparedStatement statement, Entrega entrega) throws SQLException {
        statement.setInt(1, Integer.parseInt(entrega.getIdPedido()));
        statement.setInt(2, Integer.parseInt(entrega.getIdRepartidor()));
        statement.setDate(3, entrega.getFecha() == null ? null : Date.valueOf(entrega.getFecha()));   // LocalDate -> java.sql.Date
        statement.setTime(4, entrega.getHora() == null ? null : Time.valueOf(entrega.getHora()));    // LocalTime -> java.sql.Time
    }
}
