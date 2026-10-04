package model;

/**
 * Define el comportamiento de despachar un pedido dentro del sistema
 */
public interface Despachable {
    /**
     * Indica si un pedido está listo para ser despachado
     * @param estadoEnvio
     * @return true, si el pedido está listo para ser despachado, false en caso contrario
     */
    boolean despachar (String estadoEnvio);

}
