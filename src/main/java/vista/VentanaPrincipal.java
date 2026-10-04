package vista;

import model.GestorPedidos;

import javax.swing.*;
import java.awt.*;

import util.DatosPrecargadosEjemplos;


/**
 * Ventana principal de SpeedFast. Desde aquí el usuario navega
 * hacia el registro de pedidos, el listado y la simulación de entregas.
 */
public class VentanaPrincipal extends JFrame {
    private static final String[] REPARTIDORES = {"Juan Pérez", "Camila Soto", "Luis Díaz"};
    private final GestorPedidos gestor;

    public VentanaPrincipal() {
        super("SpeedFast - Gestión de Entregas");
        this.gestor = new GestorPedidos();
        //DatosPrecargadosEjemplos.cargar(gestor);

        inicializarComponentes();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 340);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private void inicializarComponentes() {
        JPanel contenido = new JPanel(new BorderLayout(0, 20));
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 30, 25, 30));

        // Encabezado
        JLabel titulo = new JLabel("SpeedFast", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        JLabel subtitulo = new JLabel("Sistema de gestión de entregas", SwingConstants.CENTER);
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel encabezado = new JPanel(new GridLayout(2, 1));
        encabezado.add(titulo);
        encabezado.add(subtitulo);

        // Botones
        JButton btnRegistrar = crearBoton("Registrar pedido");
        JButton btnListar = crearBoton("Listado de pedidos");
        JButton btnEntregas = crearBoton("Asignar repartidor / Iniciar entrega");

        btnRegistrar.addActionListener(e -> abrirRegistro());
        btnListar.addActionListener(e -> abrirListado());
        btnEntregas.addActionListener(e -> iniciarEntregas());

        JPanel panelBotones = new JPanel(new GridLayout(3, 1, 0, 12));
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnListar);
        panelBotones.add(btnEntregas);

        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(panelBotones, BorderLayout.CENTER);
        setContentPane(contenido);
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        boton.setFocusPainted(false);
        return boton;
    }

    private void abrirRegistro() {
        new VentanaRegistroPedido(gestor).setVisible(true);

    }

    private void abrirListado() {
        new VentanaListaPedidos(gestor).setVisible(true);

    }

    private void iniciarEntregas() {
        if (gestor.hayEntregasEnCurso()) {
            JOptionPane.showMessageDialog(this,
                    "Ya hay entregas en curso. Espera a que terminen.",
                    "Entregas en curso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cantidad = gestor.iniciarEntregas(REPARTIDORES);

        if (cantidad == 0) {
            JOptionPane.showMessageDialog(this,
                    "No hay pedidos pendientes para entregar.",
                    "Sin pedidos", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Se iniciaron " + cantidad + " entrega(s) con " + REPARTIDORES.length + " repartidores.\n"
                        + "Puedes ver el avance en el listado de pedidos.",
                "Entregas iniciadas", JOptionPane.INFORMATION_MESSAGE);
        abrirListado();
    }
}

