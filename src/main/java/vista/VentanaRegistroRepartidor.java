package vista;

import database.RepartidorDAO;
import model.Repartidor;

import javax.swing.*;
import java.awt.*;

/**
 * Formulario para registrar repartidores de SpeedFast en la base de datos.
 */
public class VentanaRegistroRepartidor extends JFrame {

    private JTextField txtNombre;

    public VentanaRegistroRepartidor() {
        super("Registrar repartidor");

        inicializarComponentes();

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
        txtNombre = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.add(new JLabel("Nombre del repartidor:"));
        panel.add(txtNombre);
        return panel;
    }

    private JPanel crearPanelBotones() {
        JButton btnGuardar = new JButton("Guardar");
        JButton btnLimpiar = new JButton("Limpiar");

        btnGuardar.addActionListener(e -> guardarRepartidor());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.add(btnLimpiar);
        panel.add(btnGuardar);
        return panel;
    }

    /** Valida el nombre, guarda el repartidor en la base de datos y confirma con JOptionPane. */
    private void guardarRepartidor() {
        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El nombre es obligatorio.",
                    "Datos inválidos", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (nombre.length() > 100) {
            JOptionPane.showMessageDialog(this,
                    "El nombre no puede tener más de 100 caracteres.",
                    "Datos inválidos", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            RepartidorDAO.guardar(new Repartidor(nombre));
        } catch (RuntimeException e) {
            // Error de base de datos: se avisa y el formulario sigue abierto
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "No se pudo guardar el repartidor", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Repartidor " + nombre + " registrado correctamente.",
                "Repartidor guardado", JOptionPane.INFORMATION_MESSAGE);
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtNombre.requestFocusInWindow();
    }
}
