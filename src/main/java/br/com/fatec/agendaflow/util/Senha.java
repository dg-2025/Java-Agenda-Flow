package br.com.fatec.agendaflow.util;

import org.mindrot.jbcrypt.BCrypt;

/** RNF-02: a senha nunca é gravada em texto puro, só o hash BCrypt. */
public final class Senha {

    private Senha() {
    }

    public static String gerarHash(String senhaPura) {
        return BCrypt.hashpw(senhaPura, BCrypt.gensalt(10));
    }

    public static boolean confere(String senhaPura, String hash) {
        if (senhaPura == null || hash == null || !hash.startsWith("$2")) {
            return false;
        }
        try {
            return BCrypt.checkpw(senhaPura, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
