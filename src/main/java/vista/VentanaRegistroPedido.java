package vista;

import database.PedidoDAO;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Formulario para registrar nuevos pedidos en SpeedFast.
 * Los campos comunes siempre están visibles; los campos propios de cada
 * tipo de pedido cambian según lo elegido en el JComboBox.
 */
public class VentanaRegistroPedido extends JFrame {

    private static final String COMIDA = "Comida";
    private static final String ENCOMIENDA = "Encomienda";
    private static final String EXPRESS = "Express";

    private final GestorPedidos gestor;

    // Campos comunes
    private JTextField txtId;
    private JTextField txtDireccion;
    private JTextField txtDistancia;
    private JComboBox<String> cmbTipo;

    // Campos específicos por tipo
    private JCheckBox chkMochilaTermica;
    private JTextField txtPeso;
    private JCheckBox chkEmbalajeValido;
    private JCheckBox chkRepartidorCercano;

    private JPanel panelEspecifico;
    private CardLayout cardLayout;

    public VentanaRegistroPedido(GestorPedidos gestor) {
        super("Registrar pedido");
        this.gestor = gestor;

        inicializarComponentes();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void inicializarComponentes() {
        JPanel contenido = new JPanel(new BorderLayout(0, 15));
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        contenido.add(crearPanelComun(), BorderLayout.NORTH);
        contenido.add(crearPanelEspecifico(), BorderLayout.CENTER);
        contenido.add(crearPanelBotones(), BorderLayout.SOUTH);

        setContentPane(contenido);
    }

    /** Campos que tienen todos los pedidos: ID, dirección, distancia y tipo. */
    private JPanel crearPanelComun() {
        txtId = new JTextField(20);
        txtDireccion = new JTextField(20);
        txtDistancia = new JTextField(20);
        cmbTipo = new JComboBox<>(new String[]{COMIDA, ENCOMIENDA, EXPRESS});

        // Al cambiar el tipo, se muestra la "tarjeta" con sus campos propios
        cmbTipo.addActionListener(e -> cardLayout.show(panelEspecifico, (String) cmbTipo.getSelectedItem()));

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.add(new JLabel("ID del pedido:"));
        panel.add(txtId);
        panel.add(new JLabel("Dirección de entrega:"));
        panel.add(txtDireccion);
        panel.add(new JLabel("Distancia (km):"));
        panel.add(txtDistancia);
        panel.add(new JLabel("Tipo de pedido:"));
        panel.add(cmbTipo);
        return panel;
    }

    /** Un panel con CardLayout: una "tarjeta" de campos por cada tipo de pedido. */
    private JPanel crearPanelEspecifico() {
        chkMochilaTermica = new JCheckBox("Tiene mochila térmica", true);

        txtPeso = new JTextField(10);
        chkEmbalajeValido = new JCheckBox("Embalaje válido", true);

        chkRepartidorCercano = new JCheckBox("Repartidor cercano disponible", true);

        JPanel tarjetaComida = new JPanel(new GridLayout(1, 1));
        tarjetaComida.add(chkMochilaTermica);

        JPanel tarjetaEncomienda = new JPanel(new GridLayout(2, 2, 10, 10));
        tarjetaEncomienda.add(new JLabel("Peso (kg):"));
        tarjetaEncomienda.add(txtPeso);
        tarjetaEncomienda.add(chkEmbalajeValido);

        JPanel tarjetaExpress = new JPanel(new GridLayout(1, 1));
        tarjetaExpress.add(chkRepartidorCercano);

        cardLayout = new CardLayout();
        panelEspecifico = new JPanel(cardLayout);
        panelEspecifico.add(tarjetaComida, COMIDA);
        panelEspecifico.add(tarjetaEncomienda, ENCOMIENDA);
        panelEspecifico.add(tarjetaExpress, EXPRESS);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBorder(BorderFactory.createTitledBorder("Datos específicos del tipo"));
        contenedor.add(panelEspecifico, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel crearPanelBotones() {
        JButton btnGuardar = new JButton("Guardar");
        JButton btnLimpiar = new JButton("Limpiar");

        btnGuardar.addActionListener(e -> guardarPedido());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.add(btnLimpiar);
        panel.add(btnGuardar);
        return panel;
    }

    /** Valida, crea el pedido, lo guarda en la base de datos y confirma con JOptionPane. */
    private void guardarPedido() {
        List<String> errores = validarCampos();
        if (!errores.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Corrige lo siguiente:\n- " + String.join("\n- ", errores),
                    "Datos inválidos", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Pedido pedido = crearPedido();
        try {
            PedidoDAO.guardar(pedido);
        } catch (RuntimeException e) {
            // Error de base de datos (ID repetido, MySQL apagado, etc.): se avisa y el formulario sigue abierto
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "No se pudo guardar el pedido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        gestor.notificarCambios(); // si el listado está abierto, se refresca solo

        JOptionPane.showMessageDialog(this,
                "Pedido " + pedido.getIdPedido() + " (" + pedido.getTipoPedido() + ") registrado correctamente.",
                "Pedido guardado", JOptionPane.INFORMATION_MESSAGE);
        limpiarFormulario();
    }

    /** Revisa todos los campos y devuelve la lista de errores encontrados (vacía si todo está bien). */
    private List<String> validarCampos() {
        List<String> errores = new ArrayList<>();
        Integer id = leerEntero(txtId);

        if (txtId.getText().trim().isEmpty()) {
            errores.add("El ID es obligatorio.");
        } else if (id == null || id <= 0) {
            errores.add("El ID debe ser un número entero mayor a 0.");
        }

        String direccion = txtDireccion.getText().trim();
        if (direccion.isEmpty()) {
            errores.add("La dirección es obligatoria.");
        } else if (direccion.length() > 100) {
            errores.add("La dirección no puede tener más de 100 caracteres.");
        }

        Double distancia = leerNumero(txtDistancia);
        if (distancia == null || distancia <= 0) {
            errores.add("La distancia debe ser un número mayor a 0.");
        }

        if (ENCOMIENDA.equals(cmbTipo.getSelectedItem())) {
            Double peso = leerNumero(txtPeso);
            if (peso == null || peso <= 0) {
                errores.add("El peso debe ser un número mayor a 0.");
            }
        }
        return errores;
    }

    /** Crea la subclase de Pedido que corresponde al tipo elegido. Se llama solo después de validar. */
    private Pedido crearPedido() {
        String id = String.valueOf(leerEntero(txtId));
        String direccion = txtDireccion.getText().trim();
        double distancia = leerNumero(txtDistancia);

        String tipo = (String) cmbTipo.getSelectedItem();
        switch (tipo) {
            case COMIDA:
                return new PedidoComida(id, direccion, distancia, chkMochilaTermica.isSelected());
            case ENCOMIENDA:
                return new PedidoEncomienda(id, direccion, distancia, leerNumero(txtPeso), chkEmbalajeValido.isSelected());
            default:
                return new PedidoExpress(id, direccion, distancia, chkRepartidorCercano.isSelected());
        }
    }

    /**
     * Convierte el texto de un campo a número. Acepta coma o punto decimal.
     *
     * @return el número, o null si el texto no es un número válido
     */
    private Double leerNumero(JTextField campo) {
        try {
            return Double.parseDouble(campo.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convierte el texto de un campo a número entero.
     *
     * @return el número, o null si el texto no es un entero válido
     */
    private Integer leerEntero(JTextField campo) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtDireccion.setText("");
        txtDistancia.setText("");
        txtPeso.setText("");
        chkMochilaTermica.setSelected(true);
        chkEmbalajeValido.setSelected(true);
        chkRepartidorCercano.setSelected(true);
        cmbTipo.setSelectedIndex(0);
        txtId.requestFocusInWindow();
    }
}
