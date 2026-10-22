package com.concessionaria.api.service;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private static final int CUSTO = 12;

    public static String hashSenha(String senhaPura) {
        return BCrypt.hashpw(senhaPura, BCrypt.gensalt(CUSTO));
    }

    public static boolean verificarSenha(String senhaPura, String hashArmazenado) {
        if (senhaPura == null || hashArmazenado == null) return false;
        try {
            return BCrypt.checkpw(senhaPura, hashArmazenado);
        } catch (Exception e) {
            return false;
        }
    }
}
