package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Zona de carga compartida de SpeedFast, donde llegan los pedidos pendientes
 * y desde donde los repartidores los retiran para iniciar su entrega.
 * Los métodos agregarPedido() y retirarPedido() están sincronizados para garantizar
 * que aunque varios repartidores accedan a esta zona de carga al mismo tiempo desde hilos distintos
 * cada pedido sea retirado por uno solo y nunca se entregue dos veces.
 */

public class ZonaDeCarga {

    private List <Pedido>pedidosPendientes=new ArrayList<>();

    public synchronized void agregarPedido(Pedido p) {
        pedidosPendientes.add(p);
    }

    public synchronized Pedido retirarPedido(){
       if(!pedidosPendientes.isEmpty()){
           return pedidosPendientes.remove(0);
       }else {
           return null;
       }
    }
}
