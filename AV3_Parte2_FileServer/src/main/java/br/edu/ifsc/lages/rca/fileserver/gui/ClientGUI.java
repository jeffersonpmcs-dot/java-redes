package br.edu.ifsc.lages.rca.fileserver.gui;
import br.edu.ifsc.lages.rca.fileserver.network.TcpClient;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ClientGUI extends JFrame {
    private final TcpClient cliente = new TcpClient();
    private final JTextField ip = new JTextField("127.0.0.1",10), porta = new JTextField("6789",5);
    private final JButton conectar = new JButton("Conectar"), atualizar = new JButton("Atualizar"),
            upload = new JButton("Enviar arquivo"), download = new JButton("Baixar arquivo");
    private final DefaultListModel<String> modelo = new DefaultListModel<>();
    private final JList<String> lista = new JList<>(modelo);
    private boolean conectado;
    @FunctionalInterface private interface Operacao { List<String> executar() throws Exception; }
    public ClientGUI() {
        super("Servidor de arquivos TCP"); setSize(600,450); setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel topo=new JPanel(), acoes=new JPanel();
        for(Component c:new Component[]{new JLabel("IP"),ip,new JLabel("Porta"),porta,conectar}) topo.add(c);
        for(Component c:new Component[]{atualizar,upload,download}) acoes.add(c);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(topo,BorderLayout.NORTH); add(new JScrollPane(lista)); add(acoes,BorderLayout.SOUTH); habilitar(false);
        conectar.addActionListener(e -> {
            if(conectado) { executar(() -> { cliente.desconectar(); return List.of(); },false); return; }
            try {
                String host=ip.getText().trim(); int p=Integer.parseInt(porta.getText());
                if(host.isEmpty() || p<1 || p>65535) throw new IllegalArgumentException();
                executar(() -> { cliente.conectar(host,p); return cliente.listarArquivos(); },true);
            } catch(Exception ex) { erro("Informe IP e porta válidos."); }
        });
        atualizar.addActionListener(e -> executar(cliente::listarArquivos,true));
        upload.addActionListener(e -> {
            JFileChooser escolha=new JFileChooser();
            if(escolha.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
                File arquivo=escolha.getSelectedFile();
                executar(() -> { cliente.enviarArquivo(arquivo); return cliente.listarArquivos(); },true);
            }
        });
        download.addActionListener(e -> {
            String nome=lista.getSelectedValue(); if(nome==null) { erro("Selecione um arquivo."); return; }
            JFileChooser escolha=new JFileChooser(); escolha.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if(escolha.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
                File destino=escolha.getSelectedFile();
                if(new File(destino,nome).exists() && JOptionPane.showConfirmDialog(this,"Substituir arquivo existente?","Download",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
                executar(() -> { cliente.baixarArquivo(nome,destino); return cliente.listarArquivos(); },true);
            }
        });
    }
    private void erro(String msg) { JOptionPane.showMessageDialog(this,msg,"Aviso",JOptionPane.WARNING_MESSAGE); }
    private void habilitar(boolean ocupado) {
        conectar.setEnabled(!ocupado); atualizar.setEnabled(!ocupado && conectado);
        upload.setEnabled(!ocupado && conectado); download.setEnabled(!ocupado && conectado);
        ip.setEnabled(!ocupado && !conectado); porta.setEnabled(!ocupado && !conectado);
        conectar.setText(conectado?"Desconectar":"Conectar");
    }
    private void executar(Operacao tarefa, boolean estadoFinal) {
        habilitar(true);
        new SwingWorker<List<String>,Void>() {
            protected List<String> doInBackground() throws Exception {
                try { return tarefa.executar(); }
                catch(Exception ex) { cliente.desconectar(); throw ex; }
            }
            protected void done() {
                try { List<String> nomes=get(); conectado=estadoFinal; modelo.clear(); for(String nome:nomes) modelo.addElement(nome); }
                catch(Exception ex) { conectado=false; modelo.clear(); erro("Operação falhou: "+(ex.getCause()!=null?ex.getCause().getMessage():ex.getMessage())); }
                finally { habilitar(false); }
            }
        }.execute();
    }
}
