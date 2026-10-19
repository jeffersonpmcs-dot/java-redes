package br.edu.ifsc.lages.rca.chat.gui;

import br.edu.ifsc.lages.rca.chat.model.ChatMessage;
import br.edu.ifsc.lages.rca.chat.network.MulticastListener;
import br.edu.ifsc.lages.rca.chat.network.MulticastNetwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

// A tela implementa o MessageCallback para escutar as mensagens que chegam da rede
public class ChatGUI extends JFrame implements MulticastListener.MessageCallback {

    private MulticastNetwork rede;
    private boolean conectado = false;

    // Componentes Visuais
    private JTextField txtUsuario;
    private JTextField txtIpGrupo;
    private JTextField txtPorta;
    private JButton btnConectar;

    private JTextArea areaChat;

    private JTextField txtMensagem;
    private JButton btnEnviar;

    public ChatGUI() {
        // Inicializa o motor de rede
        rede = new MulticastNetwork();

        // Configurações básicas da Janela
        setTitle("Multicast Chat - RCA");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. PAINEL SUPERIOR (Configurações de Conexão)
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));

        painelTopo.add(new JLabel("Usuário:"));
        txtUsuario = new JTextField("", 10);
        painelTopo.add(txtUsuario);

        painelTopo.add(new JLabel("IP Grupo:"));
        txtIpGrupo = new JTextField("230.0.0.0", 8); // Diminuí o tamanho da caixa de texto
        painelTopo.add(txtIpGrupo);

        painelTopo.add(new JLabel("Porta:"));
        txtPorta = new JTextField("6789", 4); // Diminuí o tamanho da caixa de texto
        painelTopo.add(txtPorta);

        btnConectar = new JButton("Conectar");
        painelTopo.add(btnConectar); // Agora o botão tem espaço de sobra para aparecer

        add(painelTopo, BorderLayout.NORTH);

        // 2. PAINEL CENTRAL (Área de exibição das mensagens)
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollChat = new JScrollPane(areaChat);
        add(scrollChat, BorderLayout.CENTER);

        // 3. PAINEL INFERIOR (Digitar e Enviar Mensagem)
        JPanel painelBase = new JPanel(new BorderLayout(10, 10));
        // Adiciona uma margem nas bordas para ficar elegante
        painelBase.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        txtMensagem = new JTextField();
        txtMensagem.setEnabled(false); // Só libera para digitar se estiver conectado
        painelBase.add(txtMensagem, BorderLayout.CENTER);

        btnEnviar = new JButton("Enviar");
        btnEnviar.setEnabled(false);
        painelBase.add(btnEnviar, BorderLayout.EAST);

        add(painelBase, BorderLayout.SOUTH);

        // ====================================================================
        // AÇÕES DOS BOTÕES (Eventos)
        // ====================================================================

        // Botão Conectar / Desconectar
        btnConectar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alternarConexao();
            }
        });

        // Botão Enviar (também acionado ao dar ENTER no campo de texto)
        ActionListener acaoEnviar = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensagem();
            }
        };
        btnEnviar.addActionListener(acaoEnviar);
        txtMensagem.addActionListener(acaoEnviar);
    }

    // ====================================================================
    // LÓGICA DA INTERFACE
    // ====================================================================

    private void alternarConexao() {
        if (!conectado) {
            try {
                if (txtUsuario.getText().isBlank()) throw new IllegalArgumentException("Informe seu nome.");
                String ip = txtIpGrupo.getText().trim();
                int porta = Integer.parseInt(txtPorta.getText());

                // 1. Aciona a rede para entrar no grupo Multicast
                rede.conectar(ip, porta);

                // 2. Cria a Thread de Escuta passando esta tela (this) como recebedora das mensagens
                MulticastListener listener = new MulticastListener(rede.getSocket(), this);
                new Thread(listener).start();

                conectado = true;
                btnConectar.setText("Desconectar");
                txtUsuario.setEnabled(false);
                txtIpGrupo.setEnabled(false);
                txtPorta.setEnabled(false);
                txtMensagem.setEnabled(true);
                btnEnviar.setEnabled(true);

                areaChat.append(">>> Conectado ao grupo " + ip + ":" + porta + "\n");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao conectar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Desconectar
            rede.desconectar();
            conectado = false;
            btnConectar.setText("Conectar");
            txtUsuario.setEnabled(true);
            txtIpGrupo.setEnabled(true);
            txtPorta.setEnabled(true);
            txtMensagem.setEnabled(false);
            btnEnviar.setEnabled(false);

            areaChat.append(">>> Desconectado.\n");
        }
    }

    private void enviarMensagem() {
        String texto = txtMensagem.getText().trim();
        if (!texto.isEmpty() && conectado) {
            try {
                // Prepara as datas no formato exigido pelo professor
                String dataAtual = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                String horaAtual = new SimpleDateFormat("HH:mm:ss").format(new Date());
                String usuario = txtUsuario.getText();

                // Monta o objeto modelo
                ChatMessage mensagem = new ChatMessage(dataAtual, horaAtual, usuario, texto);

                // Dispara pela rede
                rede.enviarMensagem(mensagem);

                // Limpa o campo para digitar a próxima
                txtMensagem.setText("");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao enviar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ====================================================================
    // RECEBIMENTO DE MENSAGENS (Vem da Thread de Leitura)
    // ====================================================================
    @Override
    public void onMessageReceived(ChatMessage message) {
        // Como a mensagem chega de outra Thread, usamos o invokeLater para atualizar a tela visual com segurança
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String linha = String.format("[%s %s] %s: %s\n",
                        message.getDate(), message.getTime(), message.getUsername(), message.getMessage());
                areaChat.append(linha);

                // Rola o chat automaticamente para o final
                areaChat.setCaretPosition(areaChat.getDocument().getLength());
            }
        });
    }
}
