package database;

import model.EstadoPedido;
import model.FabricaPedidos;
import model.Pedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Operaciones CRUD sobre la tabla pedidos.
 */
public class PedidoDAO {

    /**
     * Inserta un pedido nuevo con su dirección, tipo y estado.
     *
     * @param pedido pedido a guardar (todavía sin id)
     * @return el id que le asignó la base de datos
     */
    public static int create(Pedido pedido) {
        ConexionDB connector = new ConexionDB();
        String sql = "INSERT INTO pedidos (direccion, tipo, estado) VALUES (?,?,?)";

        try (Connection conn = connector.conectar();
             PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, pedido.getDireccionEntrega());
            statement.setString(2, pedido.getTipoPedido().toUpperCase());   // COMIDA | ENCOMIENDA | EXPRESS
            statement.setString(3, String.valueOf(pedido.getEstadoPedido()));
            statement.executeUpdate();

            return UtilDAO.idGenerado(statement);

        } catch (SQLException e) {
            System.out.println("Error al guardar pedido [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());
            throw new RuntimeException("No se pudo guardar el pedido.\n" + UtilDAO.mensajeError(e), e);
        }
    }

    /**
     * Lee todos los pedidos, ordenados por id.
     *
     * @return lista de pedidos (vacía si no hay ninguno)
     */
    public static List<Pedido> readAll() {
        ConexionDB connector = new ConexionDB();
        String sql = "SELECT id, direccion, tipo, estado FROM pedidos ORDER BY id";
        List<Pedido> pedidos = new ArrayList<>();

        try (Connection conn = connector.conectar();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("id"));
                String direccion = rs.getString("direccion");
                String tipo = rs.getString("tipo");
                String estado = rs.getString("estado");

                EstadoPedido estadoPedido = estado == null ? null : EstadoPedido.valueOf(estado.toUpperCase());
                pedidos.add(FabricaPedidos.crear(id, direccion, tipo, estadoPedido));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar pedidos [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());
            throw new RuntimeException("No se pudieron cargar los pedidos.\n" + UtilDAO.mensajeError(e), e);
        } catch (IllegalArgumentException e) {
            System.out.println("Pedido con tipo o estado desconocido: " + e.getLocalizedMessage());
            throw new RuntimeException("Hay un pedido con un tipo o estado que el sistema no reconoce ("
                    + e.getLocalizedMessage() + ").", e);
        }

        return pedidos;
    }

    /**
     * Actualiza la dirección, el tipo y el estado de un pedido que ya existe (se busca por su id).
     *
     * @param pedido pedido con su id y los datos nuevos
     */
    public static void update(Pedido pedido) {
        ConexionDB connector = new ConexionDB();
        String sql = "UPDATE pedidos SET direccion = ?, tipo = ?, estado = ? WHERE id = ?";

        try (Connection conn = connector.conectar();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, pedido.getDireccionEntrega());
            statement.setString(2, pedido.getTipoPedido().toUpperCase());
            statement.setString(3, String.valueOf(pedido.getEstadoPedido()));
            statement.setInt(4, Integer.parseInt(pedido.getIdPedido()));

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("El pedido " + pedido.getIdPedido() + " ya no existe en la base de datos.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Id de pedido inválido: " + e.getLocalizedMessage());
            throw new RuntimeException("El ID del pedido debe ser un número entero.", e);
        } catch (SQLException e) {
            System.out.println("Error al actualizar pedido [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());
            throw new RuntimeException("No se pudo actualizar el pedido.\n" + UtilDAO.mensajeError(e), e);
        }
    }

    /**
     * Elimina un pedido. Si tiene entregas registradas, la base de datos no lo permite.
     *
     * @param id identificador del pedido a eliminar
     */
    public static void delete(String id) {
        ConexionDB connector = new ConexionDB();
        String sql = "DELETE FROM pedidos WHERE id = ?";

        try (Connection conn = connector.conectar();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, Integer.parseInt(id));

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("El pedido " + id + " ya no existe en la base de datos.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Id de pedido inválido: " + e.getLocalizedMessage());
            throw new RuntimeException("El ID del pedido debe ser un número entero.", e);
        } catch (SQLException e) {
            System.out.println("Error al eliminar pedido [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());
            throw new RuntimeException("No se pudo eliminar el pedido.\n" + UtilDAO.mensajeError(e), e);
        }
    }
}
