package br.edu.ifsc.lages.rca.fileserver;

import br.edu.ifsc.lages.rca.fileserver.gui.ClientGUI;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

public class MainClient {
    public static void main(String[] args) {
        // Aplica o tema escuro antes de renderizar
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Falha ao inicializar o FlatLaf");
        }

        // Abre a janela do cliente
        SwingUtilities.invokeLater(() -> {
            ClientGUI tela = new ClientGUI();
            tela.setVisible(true);
        });
    }
}
