package model;


import java.util.List;

/**
 * Define el comportamiento de rastrear un pedido dentro del sistema
 */
public interface Rastreable {
    List<Pedido> verHistorial();

}
