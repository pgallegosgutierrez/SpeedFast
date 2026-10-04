package main;

import vista.VentanaPrincipal;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada de SpeedFast. Solo se encarga de abrir la ventana principal.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal());
    }
}
