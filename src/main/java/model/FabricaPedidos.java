package model;

/**
 * Crea el tipo de Pedido que corresponde (comida, encomienda o express) a partir de los datos
 * que guarda la base de datos. La usan el DAO al leer y la ventana de pedidos al registrar.
 */
public class FabricaPedidos {

    /** Tipos de pedido del sistema, escritos igual que en la base de datos. */
    public static final String[] TIPOS = {"COMIDA", "ENCOMIENDA", "EXPRESS"};

    private FabricaPedidos() {
        // Clase utilitaria: no se crean instancias
    }

    /**
     * Crea el pedido usando la subclase que corresponde a su tipo. La tabla pedidos solo guarda
     * dirección, tipo y estado, así que los demás datos de cada subclase (distancia, peso, etc.)
     * quedan con valores por defecto.
     *
     * @param id        identificador del pedido (null si todavía no se guarda)
     * @param direccion dirección de entrega
     * @param tipo      COMIDA, ENCOMIENDA o EXPRESS (no importan mayúsculas ni minúsculas)
     * @param estado    estado del pedido
     * @return el pedido creado
     * @throws IllegalArgumentException si falta el tipo o el estado, o si el tipo no se reconoce
     */
    public static Pedido crear(String id, String direccion, String tipo, EstadoPedido estado) {
        if (tipo == null || estado == null) {
            throw new IllegalArgumentException("el pedido " + id + " no tiene tipo o estado");
        }

        Pedido pedido;
        switch (tipo.toUpperCase()) {
            case "COMIDA":
                pedido = new PedidoComida(id, direccion, 0, true);
                break;
            case "ENCOMIENDA":
                pedido = new PedidoEncomienda(id, direccion, 0, 0, true);
                break;
            case "EXPRESS":
                pedido = new PedidoExpress(id, direccion, 0, true);
                break;
            default:
                throw new IllegalArgumentException("tipo de pedido desconocido: " + tipo);
        }
        pedido.setEstadoPedido(estado);
        return pedido;
    }
}
