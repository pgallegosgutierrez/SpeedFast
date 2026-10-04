package model;

public class PedidoEncomienda extends Pedido {

    private static final double peso_maximo_kg = 20.0;

    private double pesoKg;
    private boolean embalajeValido;

    public PedidoEncomienda(String idPedido, String direccionEntrega, double distanciakm,double pesoKg, boolean embalajeValido) {
        super(idPedido, direccionEntrega, distanciakm,"Encomienda");
        this.pesoKg = pesoKg;
        this.embalajeValido = embalajeValido;
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public boolean isEmbalajeValido() {
        return embalajeValido;
    }

    private boolean cumplePesoMaximo() {
        return pesoKg <= peso_maximo_kg;
    }


    @Override
    public int calcularTiempoEntrega() {
        return (int) Math.round(20+ 1.5 * distanciaKm);
    }

    @Override
    public void asignarRepartidor() {
        System.out.println("Asignando repartidor...");
        if (cumplePesoMaximo() && embalajeValido) {
            System.out.println("-> Validando peso y embalaje... OK");
        } else {
            System.out.println("-> Validando peso y embalaje... ERROR (peso: " + pesoKg + " kg, embalaje válido: "
                    + embalajeValido + ")");
        }
    }


    @Override
    public void asignarRepartidor(String nombreRepartidor) {
        asignarRepartidor();
        if (cumplePesoMaximo() && embalajeValido) {
            System.out.println("-> Pedido asignado a " + nombreRepartidor);
        } else {
            System.out.println("-> No se pudo asignar a " + nombreRepartidor + ": peso o embalaje no cumplen los requisitos");
        }
    }

    @Override
    public boolean despachar(String estadoEnvio) {
        System.out.println("[Pedido " + tipoPedido + " | " + idPedido + "] Despachando... Estado: " + estadoEnvio);
        return true;
    }

    @Override
    public boolean cancelar(String motivoCancelacion) {
        System.out.println("[Pedido " + tipoPedido + " | " + idPedido + "] Cancelando... Motivo: " + motivoCancelacion);
        return true;
    }
}
