package vista;

import database.EntregaDAO;
import database.PedidoDAO;
import database.RepartidorDAO;
import model.Entrega;
import model.GestorPedidos;
import model.Pedido;
import model.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestión de entregas de SpeedFast: permite registrar, editar, eliminar y listar las entregas,
 * cada una asociada a un pedido y a un repartidor, con su fecha y su hora.
 * Los pedidos y repartidores se eligen en JComboBox cargados desde la base de datos, que se
 * refrescan solos cuando esos datos cambian. El listado se puede filtrar por pedido o por repartidor.
 */
public class VentanaEntregas extends JFrame {

    private static final String[] COLUMNAS = {"ID", "Pedido", "Repartidor", "Fecha", "Hora"};
    private static final String TODOS = "Todos";
    // Texto de ejemplo para fijar el ancho de los combos de filtro, sin importar los datos cargados
    private static final ItemCombo ANCHO_FILTRO = new ItemCombo(null, "000 - Av. Apoquindo 5670");

    private final GestorPedidos gestor;
    private final Runnable listenerCambios;

    // Formulario
    private JComboBox<ItemCombo> cmbPedido;
    private JComboBox<ItemCombo> cmbRepartidor;
    private JTextField txtFecha;
    private JTextField txtHora;

    // Listado
    private JComboBox<ItemCombo> cmbFiltroPedido;
    private JComboBox<ItemCombo> cmbFiltroRepartidor;
    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private JLabel lblTotal;

    // Últimos datos leídos de la base de datos; los filtros se aplican sobre la lista de entregas
    private List<Entrega> entregas = new ArrayList<>();
    private List<ItemCombo> itemsPedidos = new ArrayList<>();
    private List<ItemCombo> itemsRepartidores = new ArrayList<>();
    // Id de la entrega elegida en la tabla (null si no hay ninguna elegida)
    private String idSeleccionado;
    // true mientras se rellenan la tabla y los combos, para que eso no pise lo que hay en el formulario
    private boolean recargando;

    public VentanaEntregas(GestorPedidos gestor) {
        super("Gestión de entregas");
        this.gestor = gestor;

        inicializarComponentes();
        ponerFechaYHoraActuales();
        recargarDatos();

        // Escuchar cambios del gestor: así los combos y la tabla se refrescan cuando en otra ventana
        // se crea, edita o elimina un pedido o un repartidor. invokeLater los toca desde el hilo de Swing
        listenerCambios = () -> SwingUtilities.invokeLater(this::recargarDatos);
        gestor.agregarListener(listenerCambios);

        // Al cerrar la ventana, dejar de escuchar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                gestor.quitarListener(listenerCambios);
            }
        });

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(780, 580);
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

    /** Campos de una entrega: pedido, repartidor, fecha y hora. El id lo genera la base de datos. */
    private JPanel crearPanelFormulario() {
        cmbPedido = new JComboBox<>();
        cmbRepartidor = new JComboBox<>();
        txtFecha = new JTextField(12);
        txtHora = new JTextField(12);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Datos de la entrega"));
        panel.add(new JLabel("Pedido:"));
        panel.add(cmbPedido);
        panel.add(new JLabel("Repartidor:"));
        panel.add(cmbRepartidor);
        panel.add(new JLabel("Fecha (AAAA-MM-DD):"));
        panel.add(txtFecha);
        panel.add(new JLabel("Hora (HH:MM):"));
        panel.add(txtHora);
        return panel;
    }

    /** Filtros, tabla de entregas y total. */
    private JPanel crearPanelListado() {
        cmbFiltroPedido = new JComboBox<>();
        cmbFiltroRepartidor = new JComboBox<>();
        cmbFiltroPedido.setPrototypeDisplayValue(ANCHO_FILTRO);
        cmbFiltroRepartidor.setPrototypeDisplayValue(ANCHO_FILTRO);

        // BoxLayout horizontal: las etiquetas ocupan lo justo y los dos combos se reparten el resto del ancho
        JPanel panelFiltros = new JPanel();
        panelFiltros.setLayout(new BoxLayout(panelFiltros, BoxLayout.X_AXIS));
        panelFiltros.add(new JLabel("Filtrar por pedido:"));
        panelFiltros.add(Box.createHorizontalStrut(8));
        panelFiltros.add(cmbFiltroPedido);
        panelFiltros.add(Box.createHorizontalStrut(16));
        panelFiltros.add(new JLabel("y por repartidor:"));
        panelFiltros.add(Box.createHorizontalStrut(8));
        panelFiltros.add(cmbFiltroRepartidor);

        modeloTabla = UtilVista.crearModeloTabla(COLUMNAS);
        tabla = UtilVista.crearTabla(modeloTabla);

        // Ancho de cada columna (en proporción): pedido y repartidor necesitan más espacio
        int[] anchos = {50, 280, 200, 100, 90};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        lblTotal = new JLabel();

        // Al cambiar un filtro se vuelve a llenar la tabla (sin releer la base de datos)
        cmbFiltroPedido.addActionListener(e -> {
            if (!recargando) {
                mostrarEntregas();
            }
        });
        cmbFiltroRepartidor.addActionListener(e -> {
            if (!recargando) {
                mostrarEntregas();
            }
        });

        // Al elegir una fila, sus datos pasan al formulario
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !recargando) {
                cargarFilaSeleccionada();
            }
        });

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(panelFiltros, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(UtilVista.crearPanelTotal(lblTotal, this::recargarDatos), BorderLayout.SOUTH);
        return panel;
    }

    /** Copia al formulario los datos de la entrega elegida en la tabla. */
    private void cargarFilaSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        String id = String.valueOf(modeloTabla.getValueAt(fila, 0));

        for (Entrega entrega : entregas) {
            if (entrega.getId().equals(id)) {
                idSeleccionado = id;
                UtilVista.seleccionarItem(cmbPedido, entrega.getIdPedido());
                UtilVista.seleccionarItem(cmbRepartidor, entrega.getIdRepartidor());
                txtFecha.setText(entrega.getFecha() == null ? "" : entrega.getFecha().toString());
                txtHora.setText(entrega.getHora() == null ? "" : entrega.getHora().toString());
                break;
            }
        }
    }

    /**
     * Vuelve a leer de la base de datos los pedidos, los repartidores y las entregas,
     * y refresca los cuatro combos y la tabla.
     */
    private void recargarDatos() {
        List<Pedido> pedidos;
        List<Repartidor> repartidores;
        List<Entrega> entregasLeidas;
        try {
            pedidos = PedidoDAO.readAll();
            repartidores = RepartidorDAO.readAll();
            entregasLeidas = EntregaDAO.readAll();
        } catch (RuntimeException e) {
            // Error de base de datos: se avisa y la ventana queda como estaba
            UtilVista.mostrarError(this, "No se pudieron cargar los datos", e.getMessage());
            return;
        }
        entregas = entregasLeidas;

        // Texto legible para el usuario (id - dirección o id - nombre); el id queda guardado en cada ItemCombo
        itemsPedidos = new ArrayList<>();
        for (Pedido p : pedidos) {
            itemsPedidos.add(new ItemCombo(p.getIdPedido(), p.getIdPedido() + " - " + p.getDireccionEntrega()));
        }
        itemsRepartidores = new ArrayList<>();
        for (Repartidor r : repartidores) {
            itemsRepartidores.add(new ItemCombo(r.getId(), r.getId() + " - " + r.getNombre()));
        }

        recargando = true;
        UtilVista.llenarCombo(cmbPedido, itemsPedidos);
        UtilVista.llenarCombo(cmbRepartidor, itemsRepartidores);
        UtilVista.llenarCombo(cmbFiltroPedido, conOpcionTodos(itemsPedidos));
        UtilVista.llenarCombo(cmbFiltroRepartidor, conOpcionTodos(itemsRepartidores));
        recargando = false;

        mostrarEntregas();
    }

    /** Devuelve la misma lista con la opción "Todos" al principio, para los combos de filtro. */
    private List<ItemCombo> conOpcionTodos(List<ItemCombo> items) {
        List<ItemCombo> lista = new ArrayList<>();
        lista.add(new ItemCombo(null, TODOS));
        lista.addAll(items);
        return lista;
    }

    /** Llena la tabla con las entregas que cumplen los filtros elegidos. */
    private void mostrarEntregas() {
        String filtroPedido = UtilVista.idElegido(cmbFiltroPedido);            // null = todos
        String filtroRepartidor = UtilVista.idElegido(cmbFiltroRepartidor);    // null = todos

        recargando = true;
        modeloTabla.setRowCount(0);
        for (Entrega e : entregas) {
            boolean cumplePedido = filtroPedido == null || filtroPedido.equals(e.getIdPedido());
            boolean cumpleRepartidor = filtroRepartidor == null || filtroRepartidor.equals(e.getIdRepartidor());
            if (cumplePedido && cumpleRepartidor) {
                modeloTabla.addRow(new Object[]{
                        e.getId(),
                        textoDe(itemsPedidos, e.getIdPedido()),
                        textoDe(itemsRepartidores, e.getIdRepartidor()),
                        e.getFecha() == null ? "-" : e.getFecha(),
                        e.getHora() == null ? "-" : e.getHora()
                });
            }
        }
        // Si la entrega que se estaba editando sigue a la vista, queda seleccionada; si no, se limpia el formulario
        if (idSeleccionado != null && !UtilVista.seleccionarFila(tabla, idSeleccionado)) {
            limpiarFormulario();
        }
        recargando = false;

        lblTotal.setText("Mostrando " + modeloTabla.getRowCount() + " de " + entregas.size() + " entrega(s)");
    }

    /** Busca el texto legible (id - nombre o id - dirección) que corresponde a un id. */
    private String textoDe(List<ItemCombo> items, String id) {
        if (id == null) {
            return "-";
        }
        for (ItemCombo item : items) {
            if (id.equals(item.getId())) {
                return item.toString();
            }
        }
        return id;
    }

    /** @return la fecha escrita en el formulario, o null si no tiene el formato AAAA-MM-DD */
    private LocalDate leerFecha() {
        try {
            return LocalDate.parse(txtFecha.getText().trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** @return la hora escrita en el formulario, o null si no tiene el formato HH:MM */
    private LocalTime leerHora() {
        try {
            return LocalTime.parse(txtHora.getText().trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Revisa el formulario y devuelve la lista de errores encontrados (vacía si todo está bien). */
    private List<String> validarFormulario() {
        List<String> errores = new ArrayList<>();

        if (cmbPedido.getSelectedItem() == null) {
            errores.add("Debes elegir un pedido. Si la lista está vacía, registra uno primero.");
        }
        if (cmbRepartidor.getSelectedItem() == null) {
            errores.add("Debes elegir un repartidor. Si la lista está vacía, registra uno primero.");
        }

        if (txtFecha.getText().trim().isEmpty()) {
            errores.add("La fecha es obligatoria.");
        } else if (leerFecha() == null) {
            errores.add("La fecha debe ser válida y tener el formato AAAA-MM-DD, por ejemplo 2026-10-04.");
        }

        if (txtHora.getText().trim().isEmpty()) {
            errores.add("La hora es obligatoria.");
        } else if (leerHora() == null) {
            errores.add("La hora debe ser válida y tener el formato HH:MM, por ejemplo 18:30.");
        }
        return errores;
    }

    /** Crea una Entrega con los datos del formulario. Se llama solo después de validar. */
    private Entrega leerFormulario(String id) {
        return new Entrega(id, UtilVista.idElegido(cmbPedido), UtilVista.idElegido(cmbRepartidor),
                leerFecha(), leerHora());
    }

    /** Valida el formulario y guarda una entrega nueva en la base de datos. */
    private void registrar() {
        if (!UtilVista.sinErrores(this, validarFormulario())) {
            return;
        }
        Entrega entrega = leerFormulario(null);
        String resumen = "pedido " + cmbPedido.getSelectedItem() + "\nrepartidor " + cmbRepartidor.getSelectedItem();

        int id;
        try {
            id = EntregaDAO.create(entrega);
        } catch (RuntimeException e) {
            // Error de base de datos (por ejemplo, el pedido o el repartidor ya no existe): se avisa y el formulario sigue igual
            UtilVista.mostrarError(this, "No se pudo registrar la entrega", e.getMessage());
            return;
        }

        limpiarFormulario();
        gestor.notificarCambios(); // refresca esta tabla y las listas de las otras ventanas
        UtilVista.mostrarInfo(this, "Entrega registrada", "Entrega " + id + " registrada correctamente:\n" + resumen);
    }

    /** Valida el formulario y guarda los cambios de la entrega elegida en la tabla. */
    private void actualizar() {
        if (idSeleccionado == null) {
            UtilVista.mostrarError(this, "Datos inválidos", "Elige en la tabla la entrega que quieres actualizar.");
            return;
        }
        if (!UtilVista.sinErrores(this, validarFormulario())) {
            return;
        }
        String id = idSeleccionado;

        try {
            EntregaDAO.update(leerFormulario(id));
        } catch (RuntimeException e) {
            UtilVista.mostrarError(this, "No se pudo actualizar la entrega", e.getMessage());
            return;
        }

        limpiarFormulario();
        gestor.notificarCambios();
        UtilVista.mostrarInfo(this, "Entrega actualizada", "Entrega " + id + " actualizada correctamente.");
    }

    /** Pide confirmación y elimina la entrega elegida en la tabla. */
    private void eliminar() {
        if (idSeleccionado == null) {
            UtilVista.mostrarError(this, "Datos inválidos", "Elige en la tabla la entrega que quieres eliminar.");
            return;
        }
        String id = idSeleccionado;
        if (!UtilVista.confirmar(this, "¿Eliminar la entrega " + id + "? Esta acción no se puede deshacer.")) {
            return;
        }

        try {
            EntregaDAO.delete(id);
        } catch (RuntimeException e) {
            UtilVista.mostrarError(this, "No se pudo eliminar la entrega", e.getMessage());
            return;
        }

        limpiarFormulario();
        gestor.notificarCambios();
        UtilVista.mostrarInfo(this, "Entrega eliminada", "Entrega " + id + " eliminada correctamente.");
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        tabla.clearSelection();
        if (cmbPedido.getItemCount() > 0) {
            cmbPedido.setSelectedIndex(0);
        }
        if (cmbRepartidor.getItemCount() > 0) {
            cmbRepartidor.setSelectedIndex(0);
        }
        ponerFechaYHoraActuales();
    }

    /** Deja en el formulario la fecha de hoy y la hora actual (sin segundos), que el usuario puede cambiar. */
    private void ponerFechaYHoraActuales() {
        txtFecha.setText(LocalDate.now().toString());
        txtHora.setText(LocalTime.now().withSecond(0).withNano(0).toString());
    }
}
