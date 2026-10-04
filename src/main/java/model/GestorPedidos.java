package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Punto central de datos del sistema SpeedFast.
 * Mantiene en memoria la lista de pedidos registrados para que todas
 * las ventanas trabajen sobre la misma información, avisa a quien esté
 * "escuchando" cada vez que los datos cambian, y coordina las entregas.
 */
public class GestorPedidos {

    private final List<Pedido> pedidos = new ArrayList<>();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private ExecutorService ejecutor;

    public void agregarPedido(Pedido pedido) {
        pedidos.add(pedido);
        notificarCambios();
    }

    public List<Pedido> getPedidos() {
        return Collections.unmodifiableList(pedidos);
    }

    public boolean existeId(String idPedido) {
        for (Pedido p : pedidos) {
            if (p.getIdPedido().equalsIgnoreCase(idPedido)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true si todavía hay repartidores trabajando
     */
    public boolean hayEntregasEnCurso() {
        return ejecutor != null && !ejecutor.isTerminated();
    }

    /**
     * Carga los pedidos PENDIENTES en una zona de carga y lanza un hilo por
     * repartidor para entregarlos en paralelo. No espera a que terminen.
     *
     * @param nombresRepartidores nombres de los repartidores disponibles
     * @return cantidad de pedidos que se enviaron a reparto (0 si no había pendientes)
     */
    public int iniciarEntregas(String[] nombresRepartidores) {
        ZonaDeCarga zonaDeCarga = new ZonaDeCarga();
        int cantidad = 0;
        for (Pedido p : pedidos) {
            if (p.getEstadoPedido() == EstadoPedido.PENDIENTE) {
                zonaDeCarga.agregarPedido(p);
                cantidad++;
            }
        }
        if (cantidad == 0) {
            return 0;
        }

        ejecutor = Executors.newFixedThreadPool(nombresRepartidores.length);
        for (String nombre : nombresRepartidores) {
            ejecutor.execute(new Repartidor(nombre, zonaDeCarga, this));
        }
        ejecutor.shutdown(); // no acepta tareas nuevas, pero NO bloquea esperando
        return cantidad;
    }

    /**
     * Registra una acción que se ejecutará cada vez que cambien los pedidos.
     *
     * @param listener acción a ejecutar (por ejemplo, refrescar una tabla)
     */
    public void agregarListener(Runnable listener) {
        listeners.add(listener);
    }

    /**
     * Deja de avisar a un listener (por ejemplo, cuando se cierra su ventana).
     */
    public void quitarListener(Runnable listener) {
        listeners.remove(listener);
    }

    /**
     * Avisa a todos los listeners que los datos cambiaron.
     * Puede llamarse desde cualquier hilo (por ejemplo, desde un Repartidor).
     */
    public void notificarCambios() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }
}