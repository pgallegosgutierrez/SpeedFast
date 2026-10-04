package model;

/**
 * Representa un pedido express de SpeedFast
 */
public class PedidoExpress extends Pedido {
    private static final double umbral_distancia_km = 5.0;
    private static final int tiempo_base_min = 10;
    private static final int tiempo_extra_min = 5;
    private boolean repartidorCercanoDisponible;

    public PedidoExpress(String idPedido, String direccionEntrega, double distanciakm, boolean repartidorCercanoDisponible) {
        super(idPedido, direccionEntrega, distanciakm,"Express");
        this.repartidorCercanoDisponible = repartidorCercanoDisponible;
    }

    /**
     *
     * @return true si hay un repartidor cercano disponible
     */
    public boolean isRepartidorCercanoDisponible() {
        return repartidorCercanoDisponible;
    }

    /**
     * Calcula el tiempo estimado de entrega en base a la distancia, usando una fórmula propia de las encomiendas (tiempo base más un factor por
     * kilómetro recorrido).
     *
     * @return tiempo estimado de entrega, en minutos
     */
    @Override
    public int calcularTiempoEntrega() {
        int tiempo = tiempo_base_min;
        if (distanciaKm > umbral_distancia_km) {
            tiempo += tiempo_extra_min;
        }
        return tiempo;
    }

    /**
     * Asigna un repartidor a esta encomienda, validando primero que el peso y el embalaje cumplan los requisitos necesarios para el envío.
     */
    @Override
    public void asignarRepartidor() {
        System.out.println("Asignando repartidor...");
        if (repartidorCercanoDisponible) {
            System.out.println("-> Repartidor más cercano con disponibilidad inmediata encontrado.");
        } else {
            System.out.println("-> No se encontró repartidor cercano con disponibilidad inmediata.");
        }
    }

    /**
     * Asigna un repartidor a este pedido, validando primero que se cuente con mochila térmica disponible.
     */
    @Override
    public void asignarRepartidor(String nombreRepartidor) {
        asignarRepartidor();
        if (repartidorCercanoDisponible) {
            System.out.println("-> Pedido asignado a " + nombreRepartidor);
        } else {
            System.out.println("-> No se pudo asignar a " + nombreRepartidor + ": sin repartidores cercanos disponibles");
        }
    }

    /**
     * Despacha este pedido de comida, registrando su estado de envío actual.
     *
     * @param estadoEnvio estado actual del envío (por ejemplo, "En camino")
     * @return true si el pedido fue despachado correctamente
     */
    @Override
    public boolean despachar(String estadoEnvio) {
        System.out.println("[Pedido " + tipoPedido + " | " + idPedido + "] Despachando... Estado: " + estadoEnvio);
        return true;
    }

    /**
     * Cancela esta encomienda, registrando el motivo de la cancelación.
     *
     * @param motivoCancelacion motivo por el cual se cancela el pedido
     * @return true si el pedido fue cancelado correctamente
     */
    @Override
    public boolean cancelar(String motivoCancelacion) {
        System.out.println("[Pedido " + tipoPedido + " | " + idPedido + "] Cancelando... Motivo: " + motivoCancelacion);
        return true;
    }
}
