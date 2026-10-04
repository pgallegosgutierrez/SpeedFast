package vista;

import model.GestorPedidos;
import model.Pedido;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Muestra en una tabla todos los pedidos registrados en SpeedFast.
 * La tabla se actualiza sola cuando el gestor avisa que hubo cambios,
 * y también se puede refrescar manualmente con el botón.
 */
public class VentanaListaPedidos extends JFrame {

    private static final String[] COLUMNAS = {
            "ID", "Tipo", "Dirección", "Distancia (km)", "Tiempo est. (min)", "Estado", "Repartidor"
    };

    private final GestorPedidos gestor;
    private final Runnable listenerCambios;

    private DefaultTableModel modeloTabla;
    private JLabel lblTotal;

    public VentanaListaPedidos(GestorPedidos gestor) {
        super("Listado de pedidos");
        this.gestor = gestor;

        inicializarComponentes();
        refrescarTabla();

        // Escuchar cambios del gestor; invokeLater asegura que la tabla se toque desde el hilo de Swing
        listenerCambios = () -> SwingUtilities.invokeLater(this::refrescarTabla);
        gestor.agregarListener(listenerCambios);

        // Al cerrar la ventana, dejar de escuchar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                gestor.quitarListener(listenerCambios);
            }
        });

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 380);
        setLocationRelativeTo(null);
    }

    private void inicializarComponentes() {
        JPanel contenido = new JPanel(new BorderLayout(0, 10));
        contenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Modelo de la tabla: las celdas no se pueden editar a mano
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        JTable tabla = new JTable(modeloTabla);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD));
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Ancho de cada columna (en proporción): la dirección necesita más espacio
        int[] anchos = {65, 90, 240, 100, 130, 100, 115};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JLabel titulo = new JLabel("Pedidos registrados");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));

        lblTotal = new JLabel();
        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> refrescarTabla());

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(lblTotal, BorderLayout.WEST);
        panelInferior.add(btnRefrescar, BorderLayout.EAST);

        contenido.add(titulo, BorderLayout.NORTH);
        contenido.add(new JScrollPane(tabla), BorderLayout.CENTER);
        contenido.add(panelInferior, BorderLayout.SOUTH);
        setContentPane(contenido);
    }

    /** Vacía la tabla y la vuelve a llenar con los pedidos actuales del gestor. */
    private void refrescarTabla() {
        modeloTabla.setRowCount(0);
        for (Pedido p : gestor.getPedidos()) {
            modeloTabla.addRow(new Object[]{
                    p.getIdPedido(),
                    p.getTipoPedido(),
                    p.getDireccionEntrega(),
                    String.format("%.1f", p.getDistanciaKm()),
                    p.calcularTiempoEntrega(),
                    p.getEstadoPedido(),
                    p.getRepartidorAsignado() == null ? "-" : p.getRepartidorAsignado()
            });
        }
        lblTotal.setText("Total: " + modeloTabla.getRowCount() + " pedido(s)");
    }
}