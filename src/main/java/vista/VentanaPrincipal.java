package vista;

import model.GestorPedidos;

import javax.swing.*;
import java.awt.*;


/**
 * Ventana principal de SpeedFast. Desde aquí el usuario abre la gestión
 * de repartidores, la gestión de pedidos y la asignación de entregas.
 */
public class VentanaPrincipal extends JFrame {
    private final GestorPedidos gestor;
    private VentanaRepartidores ventanaRepartidores;
    private VentanaPedidos ventanaPedidos;

    public VentanaPrincipal() {
        super("SpeedFast - Gestión de Entregas");
        this.gestor = new GestorPedidos();

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
        JButton btnRepartidores = crearBoton("Gestión de repartidores");
        JButton btnPedidos = crearBoton("Gestión de pedidos");
        JButton btnEntregas = crearBoton("Asignar entrega");

        btnRepartidores.addActionListener(e -> abrirRepartidores());
        btnPedidos.addActionListener(e -> abrirPedidos());
        btnEntregas.addActionListener(e -> abrirAsignarEntrega());

        JPanel panelBotones = new JPanel(new GridLayout(3, 1, 0, 12));
        panelBotones.add(btnRepartidores);
        panelBotones.add(btnPedidos);
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

    private void abrirRepartidores() {
        // Si la ventana ya está abierta se trae al frente, en vez de abrir otra
        if (ventanaRepartidores == null || !ventanaRepartidores.isDisplayable()) {
            ventanaRepartidores = new VentanaRepartidores(gestor);
        }
        ventanaRepartidores.setVisible(true);
        ventanaRepartidores.toFront();
    }

    private void abrirPedidos() {
        if (ventanaPedidos == null || !ventanaPedidos.isDisplayable()) {
            ventanaPedidos = new VentanaPedidos(gestor);
        }
        ventanaPedidos.setVisible(true);
        ventanaPedidos.toFront();
    }

    private void abrirAsignarEntrega() {
        new VentanaAsignarEntrega().setVisible(true);

    }
}
