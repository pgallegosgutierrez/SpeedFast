package vista;

import model.GestorPedidos;

import javax.swing.*;
import java.awt.*;

import util.DatosPrecargadosEjemplos;


/**
 * Ventana principal de SpeedFast. Desde aquí el usuario navega
 * hacia el registro de pedidos y repartidores, el listado de pedidos
 * y la asignación de entregas.
 */
public class VentanaPrincipal extends JFrame {
    private final GestorPedidos gestor;
    private VentanaRepartidores ventanaRepartidores;

    public VentanaPrincipal() {
        super("SpeedFast - Gestión de Entregas");
        this.gestor = new GestorPedidos();
        //DatosPrecargadosEjemplos.cargar(gestor);

        inicializarComponentes();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 400);
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
        JButton btnRepartidor = crearBoton("Gestión de repartidores");
        JButton btnListar = crearBoton("Listado de pedidos");
        JButton btnEntregas = crearBoton("Asignar entrega");

        btnRegistrar.addActionListener(e -> abrirRegistro());
        btnRepartidor.addActionListener(e -> abrirRepartidores());
        btnListar.addActionListener(e -> abrirListado());
        btnEntregas.addActionListener(e -> abrirAsignarEntrega());

        JPanel panelBotones = new JPanel(new GridLayout(4, 1, 0, 12));
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnRepartidor);
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

    private void abrirRepartidores() {
        // Si la ventana ya está abierta se trae al frente, en vez de abrir otra
        if (ventanaRepartidores == null || !ventanaRepartidores.isDisplayable()) {
            ventanaRepartidores = new VentanaRepartidores(gestor);
        }
        ventanaRepartidores.setVisible(true);
        ventanaRepartidores.toFront();
    }

    private void abrirListado() {
        new VentanaListaPedidos(gestor).setVisible(true);

    }

    private void abrirAsignarEntrega() {
        new VentanaAsignarEntrega().setVisible(true);

    }
}

