package model;

/**
 * Representa un repartidor de SpeedFast que retira pedidos de la zona de carga
 * y los entrega uno a uno en un hilo propio, permitiendo que varios repartidores
 * trabajen en paralelo dentro del sistema.
 */
public class Repartidor implements Runnable {
    private String nombre;
    private ZonaDeCarga zonaDeCarga;
    private GestorPedidos gestor;

    /**
     * Crea un repartidor con su nombre, la zona de carga compartida y el gestor
     * al que debe avisar cuando cambie el estado de un pedido.
     *
     * @param nombre      nombre del repartidor
     * @param zonaDeCarga zona de carga compartida desde donde retira los pedidos
     * @param gestor      gestor de pedidos al que se notifican los cambios
     */
    public Repartidor(String nombre, ZonaDeCarga zonaDeCarga, GestorPedidos gestor) {
        this.nombre = nombre;
        this.zonaDeCarga = zonaDeCarga;
        this.gestor = gestor;
    }

    @Override
    public void run() {
        Pedido pedido = zonaDeCarga.retirarPedido();
        while (pedido != null) {
            System.out.println(nombre + " está retirando de la zona de carga el pedido #" + pedido.getIdPedido());

            // Asignación e inicio del reparto
            pedido.setRepartidorAsignado(nombre);
            pedido.setEstadoPedido(EstadoPedido.EN_REPARTO);
            gestor.notificarCambios();
            System.out.println("El pedido #" + pedido.getIdPedido() + " se encuentra en reparto");

            try {
                Thread.sleep(1500 + (long) (Math.random() * 2500)); // simula el viaje (1,5 a 4 segundos)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("No se pudo entregar el pedido #" + pedido.getIdPedido());
                return;
            }

            pedido.setEstadoPedido(EstadoPedido.ENTREGADO);
            gestor.notificarCambios();
            System.out.println(nombre + " entregó correctamente el pedido #" + pedido.getIdPedido());

            pedido = zonaDeCarga.retirarPedido();
        }
    }
}