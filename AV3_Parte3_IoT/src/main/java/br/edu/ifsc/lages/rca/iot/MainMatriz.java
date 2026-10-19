package br.edu.ifsc.lages.rca.iot;

import br.edu.ifsc.lages.rca.iot.gui.MatrizGUI;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

public class MainMatriz {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Falha ao inicializar o FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            MatrizGUI tela = new MatrizGUI();
            tela.setVisible(true);
        });
    }
}
