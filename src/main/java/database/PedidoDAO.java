package database;

import model.Pedido;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PedidoDAO {
    public static void guardar(Pedido pedido){
        ConexionDB connector = new ConexionDB();
        String sql = "INSERT INTO pedido (id, direccion, tipo, estado) VALUES (?,?,?,?)";
        try {
            PreparedStatement statement = connector.conectar().prepareStatement(sql);

            System.out.println(Integer.parseInt(pedido.getIdPedido()));
            statement.setInt(1, Integer.parseInt(pedido.getIdPedido()));
            statement.setString(2, pedido.getDireccionEntrega());
            statement.setString(3, pedido.getTipoPedido());
            statement.setString(4, String.valueOf(pedido.getEstadoPedido()));

            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error de conexion " + e.getLocalizedMessage());
            System.out.println("Base de datos: devdb");
            throw new RuntimeException(e);
        }
    };
}
