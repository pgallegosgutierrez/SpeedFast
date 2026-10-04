package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Código común de las ventanas de gestión: creación de tablas
 * y mensajes al usuario con JOptionPane.
 */
public class UtilVista {

    private UtilVista() {
        // Clase utilitaria: no se crean instancias
    }

    /**
     * Crea el modelo de una tabla cuyas celdas no se pueden editar a mano.
     *
     * @param columnas títulos de las columnas
     */
    public static DefaultTableModel crearModeloTabla(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    /**
     * Crea una tabla con el formato común de la aplicación:
     * encabezado en negrita y una sola fila seleccionable.
     */
    public static JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD));
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return tabla;
    }

    /**
     * Crea la fila de botones de una ventana de gestión.
     * Cada botón ejecuta la acción que se le entrega.
     */
    public static JPanel crearPanelBotones(Runnable registrar, Runnable actualizar, Runnable eliminar, Runnable limpiar) {
        JButton btnRegistrar = new JButton("Registrar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");

        btnRegistrar.addActionListener(e -> registrar.run());
        btnActualizar.addActionListener(e -> actualizar.run());
        btnEliminar.addActionListener(e -> eliminar.run());
        btnLimpiar.addActionListener(e -> limpiar.run());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.add(btnRegistrar);
        panel.add(btnActualizar);
        panel.add(btnEliminar);
        panel.add(btnLimpiar);
        return panel;
    }

    /**
     * Crea la franja inferior de un listado: el total a la izquierda y el botón "Refrescar" a la derecha.
     *
     * @param lblTotal  etiqueta donde la ventana escribe el total de filas
     * @param refrescar acción que vuelve a leer los datos de la base de datos
     */
    public static JPanel crearPanelTotal(JLabel lblTotal, Runnable refrescar) {
        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> refrescar.run());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(lblTotal, BorderLayout.WEST);
        panel.add(btnRefrescar, BorderLayout.EAST);
        return panel;
    }

    /**
     * Selecciona la fila cuyo id (primera columna) es el indicado.
     *
     * @return true si la fila existe y quedó seleccionada
     */
    public static boolean seleccionarFila(JTable tabla, String id) {
        for (int fila = 0; fila < tabla.getRowCount(); fila++) {
            if (String.valueOf(tabla.getValueAt(fila, 0)).equals(id)) {
                tabla.setRowSelectionInterval(fila, fila);
                return true;
            }
        }
        return false;
    }

    /**
     * Muestra los errores de validación de un formulario, si los hay.
     *
     * @return true si la lista está vacía (se puede continuar), false si se mostraron errores
     */
    public static boolean sinErrores(Component ventana, List<String> errores) {
        if (errores.isEmpty()) {
            return true;
        }
        JOptionPane.showMessageDialog(ventana,
                "Corrige lo siguiente:\n- " + String.join("\n- ", errores),
                "Datos inválidos", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    public static void mostrarError(Component ventana, String titulo, String mensaje) {
        JOptionPane.showMessageDialog(ventana, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }

    public static void mostrarInfo(Component ventana, String titulo, String mensaje) {
        JOptionPane.showMessageDialog(ventana, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Pide confirmación antes de una acción que no se puede deshacer.
     *
     * @return true si el usuario respondió que sí
     */
    public static boolean confirmar(Component ventana, String pregunta) {
        int respuesta = JOptionPane.showConfirmDialog(ventana, pregunta,
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return respuesta == JOptionPane.YES_OPTION;
    }
}
