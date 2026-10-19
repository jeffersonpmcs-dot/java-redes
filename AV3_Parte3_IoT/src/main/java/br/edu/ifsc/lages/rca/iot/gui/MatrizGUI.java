package br.edu.ifsc.lages.rca.iot.gui;
import br.edu.ifsc.lages.rca.iot.network.UdpClient;
import com.google.gson.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MatrizGUI extends JFrame {
    private record Filial(String ip, int porta, String usuario, String senha) {}
    private final List<Filial> filiais = new ArrayList<>();
    private final JTextField ip = new JTextField("127.0.0.1", 9), porta = new JTextField("51000", 5),
            usuario = new JTextField("admin", 6), intervalo = new JTextField("5", 3);
    private final JPasswordField senha = new JPasswordField("demo-only", 8);
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"Filial", "Dispositivo", "Estado"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabela = new JTable(modelo);
    private final JButton adicionar = new JButton("Adicionar filial"), alternar = new JButton("Ligar / desligar atuador");
    private boolean ocupado;
    private final Timer timer;
    public MatrizGUI() {
        super("Simulação IoT — Matriz");
        setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(850, 500); setLocationRelativeTo(null);
        JPanel topo = new JPanel(new GridLayout(2,1)), cadastro = new JPanel(), tempo = new JPanel();
        for (Component c : new Component[]{new JLabel("IP"),ip,new JLabel("Porta"),porta,new JLabel("Usuário"),usuario,new JLabel("Senha"),senha,adicionar}) cadastro.add(c);
        JButton aplicar = new JButton("Aplicar intervalo");
        tempo.add(new JLabel("Atualização em segundos (1–3600):")); tempo.add(intervalo); tempo.add(aplicar);
        topo.add(cadastro); topo.add(tempo); add(topo,BorderLayout.NORTH); add(new JScrollPane(tabela)); add(alternar,BorderLayout.SOUTH);
        timer = new Timer(5000, e -> atualizar()); timer.start();
        adicionar.addActionListener(e -> {
            try {
                int p = Integer.parseInt(porta.getText());
                if (p < 1 || p > 65535 || ip.getText().isBlank() || usuario.getText().isBlank()) throw new IllegalArgumentException();
                if (filiais.stream().anyMatch(f -> f.ip.equals(ip.getText().trim()) && f.porta == p)) throw new IllegalArgumentException();
                filiais.add(new Filial(ip.getText().trim(),p,usuario.getText(),new String(senha.getPassword()))); atualizar();
            } catch (Exception ex) { erro("Dados inválidos ou filial já cadastrada."); }
        });
        aplicar.addActionListener(e -> {
            try { int s = Integer.parseInt(intervalo.getText()); if(s<1 || s>3600) throw new IllegalArgumentException(); timer.setDelay(s*1000); }
            catch(Exception ex) { erro("Informe um intervalo de 1 a 3600 segundos."); }
        });
        alternar.addActionListener(e -> alterar());
    }
    private JsonObject pedido(Filial f, String cmd) {
        JsonObject req = new JsonObject(); req.addProperty("cmd",cmd); req.addProperty("user",f.usuario); req.addProperty("password",f.senha); return req;
    }
    private void ocupar(boolean valor) { ocupado=valor; adicionar.setEnabled(!valor); alternar.setEnabled(!valor); }
    private void erro(String msg) { JOptionPane.showMessageDialog(this,msg,"Aviso",JOptionPane.WARNING_MESSAGE); }
    private void atualizar() {
        if(ocupado || filiais.isEmpty()) return;
        ocupar(true); List<Filial> copia = List.copyOf(filiais);
        new SwingWorker<List<Object[]>,Void>() {
            protected List<Object[]> doInBackground() {
                List<Object[]> linhas = new ArrayList<>();
                for(Filial f:copia) {
                    String nome=f.ip+":"+f.porta;
                    try {
                        JsonObject r=JsonParser.parseString(UdpClient.enviarComando(f.ip,f.porta,pedido(f,"get_status").toString())).getAsJsonObject();
                        if(!r.has("cmd") || !"get_resp".equals(r.get("cmd").getAsString())) throw new IllegalArgumentException();
                        for(var entry:r.entrySet()) if(!entry.getKey().equals("cmd"))
                            linhas.add(new Object[]{nome,entry.getKey(),entry.getValue().getAsBoolean()?"LIGADO":"DESLIGADO"});
                    } catch(Exception ex) { linhas.add(new Object[]{nome,"SEM RESPOSTA","Verifique conexão e credenciais"}); }
                } return linhas;
            }
            protected void done() {
                try { modelo.setRowCount(0); for(Object[] row:get()) modelo.addRow(row); }
                catch(Exception ex) { erro("Falha ao atualizar painel."); } finally { ocupar(false); }
            }
        }.execute();
    }
    private void alterar() {
        int row=tabela.getSelectedRow(); if(row<0 || ocupado) return;
        String id=(String)modelo.getValueAt(row,1), nome=(String)modelo.getValueAt(row,0);
        if(!id.startsWith("actuator_")) { erro("Selecione um atuador; sensores são somente leitura."); return; }
        Filial f=filiais.stream().filter(x -> (x.ip+":"+x.porta).equals(nome)).findFirst().orElseThrow();
        JsonObject req=pedido(f,"set_req"); req.addProperty("id",id); req.addProperty("value","DESLIGADO".equals(modelo.getValueAt(row,2))); ocupar(true);
        new SwingWorker<Void,Void>() {
            protected Void doInBackground() throws Exception {
                JsonObject r=JsonParser.parseString(UdpClient.enviarComando(f.ip,f.porta,req.toString())).getAsJsonObject();
                if(r.has("error") || !r.has("cmd") || !"set_resp".equals(r.get("cmd").getAsString())) throw new IllegalStateException("Comando recusado."); return null;
            }
            protected void done() { try { get(); } catch(Exception ex) { erro("Não foi possível alterar o atuador."); } finally { ocupar(false); atualizar(); } }
        }.execute();
    }
}
