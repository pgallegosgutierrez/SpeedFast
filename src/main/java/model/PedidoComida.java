package model;

/**
 * Representa un pedido de comida de SpeedFast
 */

public class PedidoComida extends Pedido {

    private boolean tieneMochilaTermica;

    /**
     *
     * @param idPedido identificacion de un pedido
     * @param direccionEntrega direccion de la entrega
     * @param distanciakm distancia en km del lugar de entrega
     * @param tieneMochilaTermica condicion para poder entregar el pedido
     */
    public PedidoComida(String idPedido, String direccionEntrega, double distanciakm,boolean tieneMochilaTermica) {
        super(idPedido, direccionEntrega, distanciakm,"Comida");
        this.tieneMochilaTermica = tieneMochilaTermica;
    }

    /**
     * @return true si el pedido requiere mochila térmica, false en caso contrario
     */
    public boolean isTieneMochilaTermica() {
        return tieneMochilaTermica;
    }

    /**
     * Calcula el tiempo estimado de entrega en base a la distancia, usando una fórmula propia de los pedidos de comida.
     *
     * @return tiempo estimado de entrega, en minutos
     */
    @Override
    public int calcularTiempoEntrega() {
        return (int) Math.round(15 + 2 * distanciaKm);
    }

    /**
     * Asigna un repartidor a este pedido, validando primero que se cuente
     * con mochila térmica disponible.
     */
    @Override
    public void asignarRepartidor() {
        System.out.println("Asignando repartidor...");
        if (tieneMochilaTermica) {
            System.out.println("-> Verificando mochila térmica... OK");
        } else {
            System.out.println("-> Verificando mochila térmica... PENDIENTE (no se puede asignar sin mochila térmica)");
        }
    }

    /**
     * Asigna un repartidor específico a este pedido, validando primerola disponibilidad de mochila térmica.
     *
     * @param nombreRepartidor nombre del repartidor al que se intenta asignar el pedido
     */
    @Override
    public void asignarRepartidor(String nombreRepartidor) {
        asignarRepartidor();
        if (tieneMochilaTermica) {
            System.out.println("-> Pedido asignado a " + nombreRepartidor);
        } else {
            System.out.println("-> No se pudo asignar a " + nombreRepartidor + ": repartidor sin mochila térmica");
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
     * Cancela este pedido de comida, registrando el motivo de la cancelación.
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
