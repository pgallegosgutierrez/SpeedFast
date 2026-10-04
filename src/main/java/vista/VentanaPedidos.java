package vista;

import database.PedidoDAO;
import model.EstadoPedido;
import model.FabricaPedidos;
import model.GestorPedidos;
import model.Pedido;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestión de pedidos de SpeedFast: permite registrar, editar, eliminar y listar los pedidos
 * guardados en la base de datos, con filtros opcionales por tipo y por estado.
 * El formulario está arriba y el listado abajo; al elegir una fila de la tabla,
 * sus datos pasan al formulario para editarla o eliminarla.
 */
public class VentanaPedidos extends JFrame {

    private static final String[] COLUMNAS = {"ID", "Dirección", "Tipo", "Estado"};
    private static final String TODOS = "Todos";

    private final GestorPedidos gestor;
    private final Runnable listenerCambios;

    // Formulario
    private JTextField txtDireccion;
    private JComboBox<String> cmbTipo;
    private JComboBox<EstadoPedido> cmbEstado;

    // Listado
    private JComboBox<String> cmbFiltroTipo;
    private JComboBox<String> cmbFiltroEstado;
    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private JLabel lblTotal;

    // Últimos pedidos leídos de la base de datos; los filtros se aplican sobre esta lista
    private List<Pedido> pedidos = new ArrayList<>();
    // Id del pedido elegido en la tabla (null si no hay ninguno elegido)
    private String idSeleccionado;
    // true mientras se rellena la tabla, para que eso no pise lo que hay escrito en el formulario
    private boolean recargando;

    public VentanaPedidos(GestorPedidos gestor) {
        super("Gestión de pedidos");
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
        setSize(700, 540);
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

    /** Campos de un pedido: dirección, tipo y estado. El id lo genera la base de datos. */
    private JPanel crearPanelFormulario() {
        txtDireccion = new JTextField(25);
        cmbTipo = new JComboBox<>(FabricaPedidos.TIPOS);
        cmbEstado = new JComboBox<>(EstadoPedido.values());

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Datos del pedido"));
        panel.add(new JLabel("Dirección de entrega:"));
        panel.add(txtDireccion);
        panel.add(new JLabel("Tipo de pedido:"));
        panel.add(cmbTipo);
        panel.add(new JLabel("Estado:"));
        panel.add(cmbEstado);
        return panel;
    }

    /** Filtros, tabla de pedidos y total. */
    private JPanel crearPanelListado() {
        cmbFiltroTipo = new JComboBox<>();
        cmbFiltroTipo.addItem(TODOS);
        for (String tipo : FabricaPedidos.TIPOS) {
            cmbFiltroTipo.addItem(tipo);
        }

        cmbFiltroEstado = new JComboBox<>();
        cmbFiltroEstado.addItem(TODOS);
        for (EstadoPedido estado : EstadoPedido.values()) {
            cmbFiltroEstado.addItem(estado.name());
        }

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelFiltros.add(new JLabel("Filtrar por tipo:"));
        panelFiltros.add(cmbFiltroTipo);
        panelFiltros.add(new JLabel("y por estado:"));
        panelFiltros.add(cmbFiltroEstado);

        modeloTabla = UtilVista.crearModeloTabla(COLUMNAS);
        tabla = UtilVista.crearTabla(modeloTabla);

        // Ancho de cada columna (en proporción): la dirección necesita más espacio
        int[] anchos = {50, 360, 120, 120};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        lblTotal = new JLabel();

        // Al cambiar un filtro se vuelve a llenar la tabla (sin releer la base de datos)
        cmbFiltroTipo.addActionListener(e -> mostrarPedidos());
        cmbFiltroEstado.addActionListener(e -> mostrarPedidos());

        // Al elegir una fila, sus datos pasan al formulario
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !recargando) {
                cargarFilaSeleccionada();
            }
        });

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(panelFiltros, BorderLayout.NORTH);
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
        txtDireccion.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));
        cmbTipo.setSelectedItem(modeloTabla.getValueAt(fila, 2));
        cmbEstado.setSelectedItem(EstadoPedido.valueOf(String.valueOf(modeloTabla.getValueAt(fila, 3))));
    }

    /** Vuelve a leer los pedidos de la base de datos y llena la tabla. */
    private void recargarTabla() {
        try {
            pedidos = PedidoDAO.readAll();
        } catch (RuntimeException e) {
            // Error de base de datos: se avisa y la tabla queda como estaba
            UtilVista.mostrarError(this, "No se pudieron cargar los pedidos", e.getMessage());
            return;
        }
        mostrarPedidos();
    }

    /** Llena la tabla con los pedidos que cumplen los filtros elegidos. */
    private void mostrarPedidos() {
        String filtroTipo = (String) cmbFiltroTipo.getSelectedItem();
        String filtroEstado = (String) cmbFiltroEstado.getSelectedItem();

        recargando = true;
        modeloTabla.setRowCount(0);
        for (Pedido p : pedidos) {
            String tipo = p.getTipoPedido().toUpperCase();
            String estado = String.valueOf(p.getEstadoPedido());

            boolean cumpleTipo = TODOS.equals(filtroTipo) || tipo.equals(filtroTipo);
            boolean cumpleEstado = TODOS.equals(filtroEstado) || estado.equals(filtroEstado);
            if (cumpleTipo && cumpleEstado) {
                modeloTabla.addRow(new Object[]{p.getIdPedido(), p.getDireccionEntrega(), tipo, estado});
            }
        }
        // Si el pedido que se estaba editando sigue a la vista, queda seleccionado; si no, se limpia el formulario
        if (idSeleccionado != null && !UtilVista.seleccionarFila(tabla, idSeleccionado)) {
            limpiarFormulario();
        }
        recargando = false;

        lblTotal.setText("Mostrando " + modeloTabla.getRowCount() + " de " + pedidos.size() + " pedido(s)");
    }

    /** Revisa el formulario y devuelve la lista de errores encontrados (vacía si todo está bien). */
    private List<String> validarFormulario() {
        List<String> errores = new ArrayList<>();
        String direccion = txtDireccion.getText().trim();

        if (direccion.isEmpty()) {
            errores.add("La dirección es obligatoria.");
        } else if (direccion.length() > 100) {
            errores.add("La dirección no puede tener más de 100 caracteres.");
        } else if (!direccion.matches(".*\\p{L}.*")) {
            // \p{L} = cualquier letra: una dirección no puede ser solo números o símbolos
            errores.add("La dirección debe incluir el nombre de la calle.");
        }

        if (cmbTipo.getSelectedItem() == null) {
            errores.add("Debes elegir el tipo de pedido.");
        }
        if (cmbEstado.getSelectedItem() == null) {
            errores.add("Debes elegir el estado del pedido.");
        }
        return errores;
    }

    /** Crea un Pedido con los datos del formulario. Se llama solo después de validar. */
    private Pedido leerFormulario(String id) {
        String direccion = txtDireccion.getText().trim();
        String tipo = (String) cmbTipo.getSelectedItem();
        EstadoPedido estado = (EstadoPedido) cmbEstado.getSelectedItem();
        return FabricaPedidos.crear(id, direccion, tipo, estado);
    }

    /** Valida el formulario y guarda un pedido nuevo en la base de datos. */
    private void registrar() {
        if (!UtilVista.sinErrores(this, validarFormulario())) {
            return;
        }

        int id;
        try {
            id = PedidoDAO.create(leerFormulario(null));
        } catch (RuntimeException e) {
            // Error de base de datos: se avisa y el formulario conserva lo escrito
            UtilVista.mostrarError(this, "No se pudo registrar el pedido", e.getMessage());
            return;
        }

        limpiarFormulario();
        gestor.notificarCambios(); // refresca esta tabla y las listas de las otras ventanas
        UtilVista.mostrarInfo(this, "Pedido registrado", "Pedido " + id + " registrado correctamente.");
    }

    /** Valida el formulario y guarda los cambios del pedido elegido en la tabla. */
    private void actualizar() {
        if (idSeleccionado == null) {
            UtilVista.mostrarError(this, "Datos inválidos", "Elige en la tabla el pedido que quieres actualizar.");
            return;
        }
        if (!UtilVista.sinErrores(this, validarFormulario())) {
            return;
        }
        String id = idSeleccionado;

        try {
            PedidoDAO.update(leerFormulario(id));
        } catch (RuntimeException e) {
            UtilVista.mostrarError(this, "No se pudo actualizar el pedido", e.getMessage());
            return;
        }

        limpiarFormulario();
        gestor.notificarCambios();
        UtilVista.mostrarInfo(this, "Pedido actualizado", "Pedido " + id + " actualizado correctamente.");
    }

    /** Pide confirmación y elimina el pedido elegido en la tabla. */
    private void eliminar() {
        if (idSeleccionado == null) {
            UtilVista.mostrarError(this, "Datos inválidos", "Elige en la tabla el pedido que quieres eliminar.");
            return;
        }
        String id = idSeleccionado;
        if (!UtilVista.confirmar(this, "¿Eliminar el pedido " + id + "? Esta acción no se puede deshacer.")) {
            return;
        }

        try {
            PedidoDAO.delete(id);
        } catch (RuntimeException e) {
            // Por ejemplo, el pedido tiene entregas registradas
            UtilVista.mostrarError(this, "No se pudo eliminar el pedido", e.getMessage());
            return;
        }

        limpiarFormulario();
        gestor.notificarCambios();
        UtilVista.mostrarInfo(this, "Pedido eliminado", "Pedido " + id + " eliminado correctamente.");
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        tabla.clearSelection();
        txtDireccion.setText("");
        cmbTipo.setSelectedIndex(0);
        cmbEstado.setSelectedItem(EstadoPedido.PENDIENTE);
        txtDireccion.requestFocusInWindow();
    }
}
