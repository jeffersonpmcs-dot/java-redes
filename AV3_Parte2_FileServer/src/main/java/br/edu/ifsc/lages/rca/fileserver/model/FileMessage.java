package br.edu.ifsc.lages.rca.fileserver.model;

import java.util.List;

public class FileMessage {

    // Chaves exatas do layout JSON exigido no PDF
    private String cmd;
    private String file;
    private String hash;
    private String value; // Aqui vai o arquivo convertido em Base64
    private String status; // ok ou fail
    private List<String> files; // Lista de arquivos armazenados

    public FileMessage() {
        // Construtor vazio obrigatório para o Gson funcionar
    }

    // =========================================================
    // GETTERS E SETTERS
    // =========================================================
    public String getCmd() { return cmd; }
    public void setCmd(String cmd) { this.cmd = cmd; }

    public String getFile() { return file; }
    public void setFile(String file) { this.file = file; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getFiles() { return files; }
    public void setFiles(List<String> files) { this.files = files; }
}
