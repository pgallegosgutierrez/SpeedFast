package database;

import model.EstadoPedido;
import model.Pedido;
import model.PedidoComida;
import model.PedidoEncomienda;
import model.PedidoExpress;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    public static void guardar(Pedido pedido) {
        ConexionDB connector = new ConexionDB();
        String sql = "INSERT INTO pedidos (id, direccion, tipo, estado) VALUES (?,?,?,?)";

        try (Connection conn = connector.conectar();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, Integer.parseInt(pedido.getIdPedido()));
            statement.setString(2, pedido.getDireccionEntrega());
            statement.setString(3, pedido.getTipoPedido().toUpperCase());   // COMIDA | ENCOMIENDA | EXPRESS
            statement.setString(4, String.valueOf(pedido.getEstadoPedido()));

            statement.executeUpdate();

        } catch (NumberFormatException e) {
            System.out.println("Id de pedido inválido: " + e.getLocalizedMessage());
            throw new RuntimeException("El ID del pedido debe ser un número entero.", e);
        } catch (SQLException e) {
            System.out.println("Error al guardar pedido [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());
            if (e.getErrorCode() == 1062) {
                // Error 1062 = la clave primaria ya existe (id repetido)
                throw new RuntimeException("Ya existe un pedido con el ID " + pedido.getIdPedido() + ".", e);
            }
            throw new RuntimeException("No se pudo guardar el pedido.\n" + e.getLocalizedMessage(), e);
        }
    }

    public static List<Pedido> listarTodos() {
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

                Pedido pedido = crearPedido(id, direccion, tipo);
                pedido.setEstadoPedido(EstadoPedido.valueOf(estado.toUpperCase()));
                pedidos.add(pedido);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar pedidos [" + e.getErrorCode() + "]: " + e.getLocalizedMessage());
            throw new RuntimeException("No se pudieron cargar los pedidos.\n" + e.getLocalizedMessage(), e);
        } catch (IllegalArgumentException e) {
            System.out.println("Pedido con tipo o estado desconocido: " + e.getLocalizedMessage());
            throw new RuntimeException("Hay un pedido con un tipo o estado que el sistema no reconoce ("
                    + e.getLocalizedMessage() + ").", e);
        }

        return pedidos;
    }

    /**
     * Crea la subclase de Pedido que corresponde al tipo guardado. La tabla pedidos solo tiene
     * id, dirección, tipo y estado, así que los demás datos quedan con valores por defecto.
     */
    private static Pedido crearPedido(String id, String direccion, String tipo) {
        switch (tipo.toUpperCase()) {
            case "COMIDA":
                return new PedidoComida(id, direccion, 0, true);
            case "ENCOMIENDA":
                return new PedidoEncomienda(id, direccion, 0, 0, true);
            case "EXPRESS":
                return new PedidoExpress(id, direccion, 0, true);
            default:
                throw new IllegalArgumentException("tipo de pedido desconocido: " + tipo);
        }
    }
}
