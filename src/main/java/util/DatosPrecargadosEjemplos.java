package util;

import model.GestorPedidos;
import model.PedidoComida;
import model.PedidoEncomienda;
import model.PedidoExpress;

/**
 * Pedidos de ejemplo para que el sistema no parta vacío.
 * Útil para probar y para demostrar la aplicación.
 */

public class DatosPrecargadosEjemplos {

    private DatosPrecargadosEjemplos() {
        // Clase utilitaria: no se crean instancias
    }

    /**
     * Agrega un conjunto de pedidos de ejemplo al gestor indicado.
     *
     * @param gestor gestor donde se cargarán los pedidos
     */
    public static void cargar(GestorPedidos gestor) {
        gestor.agregarPedido(new PedidoComida("1", "Av. Providencia 1234, Providencia", 10.5, true));
        gestor.agregarPedido(new PedidoComida("2", "Av. Salvador 334, Providencia", 12, true));
        gestor.agregarPedido(new PedidoComida("3", "Av. Apoquindo 124, Las Condes", 15, true));
        gestor.agregarPedido(new PedidoEncomienda("4", "Av. Apoquindo 5670, Las Condes", 21.4, 2.5, true));
        gestor.agregarPedido(new PedidoEncomienda("5", "Av. Simón Bolívar 3500, Ñuñoa", 8.2, 4.0, true));
        gestor.agregarPedido(new PedidoExpress("6", "Calle Merced 890, Santiago Centro", 5.0, true));
        gestor.agregarPedido(new PedidoExpress("7", "Vecinal 4675, San Joaquín", 6.0, true));
    }
}