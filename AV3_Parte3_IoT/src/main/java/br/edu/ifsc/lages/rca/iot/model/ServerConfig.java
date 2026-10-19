package br.edu.ifsc.lages.rca.iot.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ServerConfig {

    private String filial;
    private int port;

    @SerializedName("admin user")
    private String adminUser;

    @SerializedName("admin pass")
    private String adminPass;

    private List<String> id;

    public String getFilial() { return filial; }
    public int getPort() { return port; }
    public String getAdminUser() { return adminUser; }
    public String getAdminPass() { return adminPass; }
    public List<String> getId() { return id; }
}
