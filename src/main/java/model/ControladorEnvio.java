package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Controla el historial de pedidos despachados dentro del sistema SpeedFast, permitiendo registrar y consultar las entregas realizadas.
 */
public class ControladorEnvio implements Rastreable {
    private List<Pedido> historial = new ArrayList<>();

    /**
     * Registra un pedido en el historial de entregas.
     *
     * @param pedido pedido que se agrega al historial
     */
    public void registrarEntrega(Pedido pedido) {
        historial.add(pedido);
    }

    /**
     * @return la lista de pedidos registrados en el historial de entregas
     */
    @Override
    public List<Pedido> verHistorial() {
        return historial;
    }


}
