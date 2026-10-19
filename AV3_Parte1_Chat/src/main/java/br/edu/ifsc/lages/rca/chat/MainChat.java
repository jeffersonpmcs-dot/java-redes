package br.edu.ifsc.lages.rca.chat;

import br.edu.ifsc.lages.rca.chat.gui.ChatGUI;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

public class MainChat {
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Falha ao inicializar o FlatLaf");
        }

        // 2. Abre a interface gráfica com segurança
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ChatGUI tela = new ChatGUI();
                tela.setVisible(true);
            }
        });
    }
}
