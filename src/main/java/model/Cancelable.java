package model;

/**
 * Define el comportamiento de cancelar un pedido dentro del sistema
 */
public interface Cancelable {

    /**
     * Intenta cancelar un pedido
     * @param motivoCancelacion
     * @return true si el pedido fue cancelado correctamente, false en caso contrario
     */
    boolean cancelar (String motivoCancelacion);

}
