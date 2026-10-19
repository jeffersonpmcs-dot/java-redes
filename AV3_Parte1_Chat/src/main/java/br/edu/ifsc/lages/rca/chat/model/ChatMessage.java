package br.edu.ifsc.lages.rca.chat.model;

public class ChatMessage {

    // Nomes exatos exigidos pelo layout do professor Robson
    private String date;
    private String time;
    private String username;
    private String message;

    // Construtor vazio (obrigatório para o Gson conseguir instanciar o objeto ao receber dados)
    public ChatMessage() {
    }

    // Construtor completo para facilitar a criação da mensagem na hora de enviar
    public ChatMessage(String date, String time, String username, String message) {
        this.date = date;
        this.time = time;
        this.username = username;
        this.message = message;
    }

    // =========================================================
    // GETTERS E SETTERS
    // =========================================================
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
