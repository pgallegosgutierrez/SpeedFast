package model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa la entrega de un pedido asignada a un repartidor,
 * junto con la fecha y la hora en que se registró.
 */
public class Entrega {
    private String id;
    private String idPedido;
    private String idRepartidor;
    private LocalDate fecha;
    private LocalTime hora;

    /**
     * Crea una entrega con la fecha y la hora actuales.
     *
     * @param idPedido     identificador del pedido que se entrega
     * @param idRepartidor identificador del repartidor que realiza la entrega
     */
    public Entrega(String idPedido, String idRepartidor) {
        // La hora se guarda sin nanosegundos: la columna TIME de MySQL llega hasta los segundos
        this(idPedido, idRepartidor, LocalDate.now(), LocalTime.now().withNano(0));
    }

    /**
     * Crea una entrega indicando su fecha y su hora.
     *
     * @param idPedido     identificador del pedido que se entrega
     * @param idRepartidor identificador del repartidor que realiza la entrega
     * @param fecha        fecha de la entrega
     * @param hora         hora de la entrega
     */
    public Entrega(String idPedido, String idRepartidor, LocalDate fecha, LocalTime hora) {
        this.idPedido = idPedido;
        this.idRepartidor = idRepartidor;
        this.fecha = fecha;
        this.hora = hora;
    }

    /**
     * Crea una entrega que ya existe en la base de datos.
     *
     * @param id           identificador de la entrega en la base de datos
     * @param idPedido     identificador del pedido que se entrega
     * @param idRepartidor identificador del repartidor que realiza la entrega
     * @param fecha        fecha de la entrega
     * @param hora         hora de la entrega
     */
    public Entrega(String id, String idPedido, String idRepartidor, LocalDate fecha, LocalTime hora) {
        this(idPedido, idRepartidor, fecha, hora);
        this.id = id;
    }

    /**
     *
     * @return el identificador de la entrega en la base de datos, o null si aún no se guarda
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return el identificador del pedido que se entrega
     */
    public String getIdPedido() {
        return idPedido;
    }

    /**
     *
     * @return el identificador del repartidor que realiza la entrega
     */
    public String getIdRepartidor() {
        return idRepartidor;
    }

    /**
     *
     * @return la fecha de la entrega
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     *
     * @return la hora de la entrega
     */
    public LocalTime getHora() {
        return hora;
    }
}
