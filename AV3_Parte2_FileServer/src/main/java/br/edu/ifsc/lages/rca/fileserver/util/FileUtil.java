package br.edu.ifsc.lages.rca.fileserver.util;

import java.security.MessageDigest;
import java.util.Base64;

public class FileUtil {

    // =========================================================================
    // 1. CONVERSÃO PARA BASE64 (Transformar Arquivo em Texto)
    // =========================================================================
    public static String encodeBase64(byte[] fileBytes) {
        // Pega os bytes puros do arquivo (foto, pdf, etc) e transforma numa String segura para viajar no JSON
        return Base64.getEncoder().encodeToString(fileBytes);
    }

    public static byte[] decodeBase64(String base64String) {
        // Faz o caminho inverso: pega o texto que chegou do JSON e transforma de volta em bytes
        return Base64.getDecoder().decode(base64String);
    }

    // =========================================================================
    // 2. CÁLCULO DO HASH SHA-256 (Assinatura de Integridade)
    // =========================================================================
    public static String calculateSHA256(byte[] fileBytes) throws Exception {
        // Instancia o algoritmo criptográfico SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // Gera a assinatura matemática (hash) baseada nos bytes do arquivo
        byte[] hashBytes = digest.digest(fileBytes);

        // Converte os bytes do hash para o formato Hexadecimal (padrão legível de hashes)
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0'); // Adiciona um zero à esquerda se for um único caractere
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }
}
