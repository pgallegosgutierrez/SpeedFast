package vista;

import database.RepartidorDAO;
import model.GestorPedidos;
import model.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestión de repartidores de SpeedFast: permite registrar, editar, eliminar y listar
 * los repartidores guardados en la base de datos.
 * El formulario está arriba y el listado abajo; al elegir una fila de la tabla,
 * sus datos pasan al formulario para editarla o eliminarla.
 */
public class VentanaRepartidores extends JFrame {

    private static final String[] COLUMNAS = {"ID", "Nombre"};

    private final GestorPedidos gestor;
    private final Runnable listenerCambios;

    private JTextField txtNombre;
    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private JLabel lblTotal;

    // Id del repartidor elegido en la tabla (null si no hay ninguno elegido)
    private String idSeleccionado;
    // true mientras se rellena la tabla, para que eso no pise lo que hay escrito en el formulario
    private boolean recargando;

    public VentanaRepartidores(GestorPedidos gestor) {
        super("Gestión de repartidores");
        this.gestor = gestor;

        inicializarComponentes();
        recargarTabla();

        // Escuchar cambios del gestor; invokeLater asegura que la tabla se toque desde el hilo de Swing
        listenerCambios = () -> SwingUtilities.invokeLater(this::recargarTabla);
        gestor.agregarListener(listenerCambios);

        // Al cerrar la ventana, dejar de escuchar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                gestor.quitarListener(listenerCambios);
            }
        });

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(560, 460);
        setLocationRelativeTo(null);
    }

    private void inicializarComponentes() {
        JPanel contenido = new JPanel(new BorderLayout(0, 12));
        contenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel panelSuperior = new JPanel(new BorderLayout(0, 10));
        panelSuperior.add(crearPanelFormulario(), BorderLayout.CENTER);
        panelSuperior.add(UtilVista.crearPanelBotones(
                this::registrar, this::actualizar, this::eliminar, this::limpiarFormulario), BorderLayout.SOUTH);

        contenido.add(panelSuperior, BorderLayout.NORTH);
        contenido.add(crearPanelListado(), BorderLayout.CENTER);
        setContentPane(contenido);
    }

    private JPanel crearPanelFormulario() {
        txtNombre = new JTextField(25);

        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Datos del repartidor"));
        panel.add(new JLabel("Nombre:"), BorderLayout.WEST);
        panel.add(txtNombre, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelListado() {
        modeloTabla = UtilVista.crearModeloTabla(COLUMNAS);
        tabla = UtilVista.crearTabla(modeloTabla);
        tabla.getColumnModel().getColumn(0).setMaxWidth(80);

        // Al elegir una fila, sus datos pasan al formulario
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !recargando) {
                cargarFilaSeleccionada();
            }
        });

        lblTotal = new JLabel();

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(UtilVista.crearPanelTotal(lblTotal, this::recargarTabla), BorderLayout.SOUTH);
        return panel;
    }

    /** Copia al formulario los datos de la fila elegida en la tabla. */
    private void cargarFilaSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        idSeleccionado = String.valueOf(modeloTabla.getValueAt(fila, 0));
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));
    }

    /** Vuelve a leer los repartidores de la base de datos y llena la tabla. */
    private void recargarTabla() {
        List<Repartidor> repartidores;
        try {
            repartidores = RepartidorDAO.readAll();
        } catch (RuntimeException e) {
            // Error de base de datos: se avisa y la tabla queda como estaba
            UtilVista.mostrarError(this, "No se pudieron cargar los repartidores", e.getMessage());
            return;
        }

        recargando = true;
        modeloTabla.setRowCount(0);
        for (Repartidor r : repartidores) {
            modeloTabla.addRow(new Object[]{r.getId(), r.getNombre()});
        }
        // Si el repartidor que se estaba editando sigue existiendo, queda seleccionado; si no, se limpia el formulario
        if (idSeleccionado != null && !UtilVista.seleccionarFila(tabla, idSeleccionado)) {
            limpiarFormulario();
        }
        recargando = false;

        lblTotal.setText("Total: " + repartidores.size() + " repartidor(es)");
    }

    /** Revisa el formulario y devuelve la lista de errores encontrados (vacía si todo está bien). */
    private List<String> validarFormulario() {
        List<String> errores = new ArrayList<>();
        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            errores.add("El nombre es obligatorio.");
        } else if (nombre.length() > 100) {
            errores.add("El nombre no puede tener más de 100 caracteres.");
        } else if (!nombre.matches("[\\p{L} .'-]+")) {
            // \p{L} = cualquier letra, con o sin tilde
            errores.add("El nombre solo puede tener letras y espacios.");
        }
        return errores;
    }

    /** Valida el formulario y guarda un repartidor nuevo en la base de datos. */
    private void registrar() {
        if (!UtilVista.sinErrores(this, validarFormulario())) {
            return;
        }
        String nombre = txtNombre.getText().trim();

        try {
            RepartidorDAO.create(new Repartidor(nombre));
        } catch (RuntimeException e) {
            // Error de base de datos: se avisa y el formulario conserva lo escrito
            UtilVista.mostrarError(this, "No se pudo registrar el repartidor", e.getMessage());
            return;
        }

        limpiarFormulario();
        gestor.notificarCambios(); // refresca esta tabla y las listas de las otras ventanas
        UtilVista.mostrarInfo(this, "Repartidor registrado", "Repartidor " + nombre + " registrado correctamente.");
    }

    /** Valida el formulario y guarda los cambios del repartidor elegido en la tabla. */
    private void actualizar() {
        if (idSeleccionado == null) {
            UtilVista.mostrarError(this, "Datos inválidos", "Elige en la tabla el repartidor que quieres actualizar.");
            return;
        }
        if (!UtilVista.sinErrores(this, validarFormulario())) {
            return;
        }
        String id = idSeleccionado;
        String nombre = txtNombre.getText().trim();

        try {
            RepartidorDAO.update(new Repartidor(id, nombre));
        } catch (RuntimeException e) {
            UtilVista.mostrarError(this, "No se pudo actualizar el repartidor", e.getMessage());
            return;
        }

        limpiarFormulario();
        gestor.notificarCambios();
        UtilVista.mostrarInfo(this, "Repartidor actualizado", "Repartidor " + id + " actualizado correctamente.");
    }

    /** Pide confirmación y elimina el repartidor elegido en la tabla. */
    private void eliminar() {
        if (idSeleccionado == null) {
            UtilVista.mostrarError(this, "Datos inválidos", "Elige en la tabla el repartidor que quieres eliminar.");
            return;
        }
        String id = idSeleccionado;
        if (!UtilVista.confirmar(this, "¿Eliminar al repartidor " + id + "? Esta acción no se puede deshacer.")) {
            return;
        }

        try {
            RepartidorDAO.delete(id);
        } catch (RuntimeException e) {
            // Por ejemplo, el repartidor tiene entregas registradas
            UtilVista.mostrarError(this, "No se pudo eliminar el repartidor", e.getMessage());
            return;
        }

        limpiarFormulario();
        gestor.notificarCambios();
        UtilVista.mostrarInfo(this, "Repartidor eliminado", "Repartidor " + id + " eliminado correctamente.");
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        tabla.clearSelection();
        txtNombre.setText("");
        txtNombre.requestFocusInWindow();
    }
}
