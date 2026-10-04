package vista;

import database.EntregaDAO;
import database.PedidoDAO;
import database.RepartidorDAO;
import model.Entrega;
import model.Pedido;
import model.Repartidor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Permite asignar la entrega de un pedido a un repartidor.
 * Los pedidos y los repartidores se cargan desde la base de datos, y la entrega
 * se guarda con la fecha y la hora del momento en que se asigna.
 */
public class VentanaAsignarEntrega extends JFrame {

    // Listas en el mismo orden que los JComboBox: la posición elegida indica el objeto
    private List<Pedido> pedidos = new ArrayList<>();
    private List<Repartidor> repartidores = new ArrayList<>();

    private JComboBox<String> cmbPedido;
    private JComboBox<String> cmbRepartidor;

    public VentanaAsignarEntrega() {
        super("Asignar entrega");

        inicializarComponentes();
        cargarListas();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void inicializarComponentes() {
        JPanel contenido = new JPanel(new BorderLayout(0, 15));
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        contenido.add(crearPanelCampos(), BorderLayout.CENTER);
        contenido.add(crearPanelBotones(), BorderLayout.SOUTH);

        setContentPane(contenido);
    }

    private JPanel crearPanelCampos() {
        cmbPedido = new JComboBox<>();
        cmbRepartidor = new JComboBox<>();

        // Ancho fijo, para que la ventana no cambie de tamaño según los datos cargados
        String textoDeEjemplo = "000 - Encomienda - Av. Apoquindo 5670, Las Condes";
        cmbPedido.setPrototypeDisplayValue(textoDeEjemplo);
        cmbRepartidor.setPrototypeDisplayValue(textoDeEjemplo);

        JPanel panelPedido = new JPanel(new BorderLayout(0, 5));
        panelPedido.add(new JLabel("Pedido:"), BorderLayout.NORTH);
        panelPedido.add(cmbPedido, BorderLayout.CENTER);

        JPanel panelRepartidor = new JPanel(new BorderLayout(0, 5));
        panelRepartidor.add(new JLabel("Repartidor:"), BorderLayout.NORTH);
        panelRepartidor.add(cmbRepartidor, BorderLayout.CENTER);

        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 12));
        panel.add(panelPedido);
        panel.add(panelRepartidor);
        return panel;
    }

    private JPanel crearPanelBotones() {
        JButton btnRecargar = new JButton("Recargar listas");
        JButton btnAsignar = new JButton("Asignar entrega");

        btnRecargar.addActionListener(e -> cargarListas());
        btnAsignar.addActionListener(e -> asignarEntrega());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.add(btnRecargar);
        panel.add(btnAsignar);
        return panel;
    }

    /** Lee los pedidos y repartidores de la base de datos y llena los JComboBox. */
    private void cargarListas() {
        List<Pedido> pedidosLeidos;
        List<Repartidor> repartidoresLeidos;
        try {
            pedidosLeidos = PedidoDAO.listarTodos();
            repartidoresLeidos = RepartidorDAO.listarTodos();
        } catch (RuntimeException e) {
            // Error de base de datos: se avisa y las listas quedan como estaban
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "No se pudieron cargar los datos", JOptionPane.ERROR_MESSAGE);
            return;
        }
        pedidos = pedidosLeidos;
        repartidores = repartidoresLeidos;

        cmbPedido.removeAllItems();
        for (Pedido p : pedidos) {
            cmbPedido.addItem(p.getIdPedido() + " - " + p.getTipoPedido() + " - " + p.getDireccionEntrega());
        }

        cmbRepartidor.removeAllItems();
        for (Repartidor r : repartidores) {
            cmbRepartidor.addItem(r.getId() + " - " + r.getNombre());
        }
    }

    /** Valida la selección, guarda la entrega en la base de datos y confirma con JOptionPane. */
    private void asignarEntrega() {
        int posicionPedido = cmbPedido.getSelectedIndex();
        int posicionRepartidor = cmbRepartidor.getSelectedIndex();

        List<String> errores = new ArrayList<>();
        if (posicionPedido < 0) {
            errores.add("Debes elegir un pedido. Si la lista está vacía, registra uno primero.");
        }
        if (posicionRepartidor < 0) {
            errores.add("Debes elegir un repartidor. Si la lista está vacía, registra uno primero.");
        }
        if (!errores.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Corrige lo siguiente:\n- " + String.join("\n- ", errores),
                    "Datos inválidos", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Pedido pedido = pedidos.get(posicionPedido);
        Repartidor repartidor = repartidores.get(posicionRepartidor);
        Entrega entrega = new Entrega(pedido.getIdPedido(), repartidor.getId());

        try {
            EntregaDAO.guardar(entrega);
        } catch (RuntimeException e) {
            // Error de base de datos (por ejemplo, el pedido o el repartidor ya no existe): se avisa y la ventana sigue abierta
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "No se pudo asignar la entrega", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Pedido " + pedido.getIdPedido() + " asignado a " + repartidor.getNombre() + ".\n"
                        + "Fecha: " + entrega.getFecha() + "   Hora: " + entrega.getHora(),
                "Entrega registrada", JOptionPane.INFORMATION_MESSAGE);
    }
}
