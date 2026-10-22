package com.concessionaria.api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Gera e valida tokens JWT.
 *
 * Um JWT tem 3 partes (header.payload.signature). O "payload" carrega
 * informações (aqui: username, papel e funcionarioId) e a "signature"
 * garante que ninguém alterou esse conteúdo sem saber o segredo (JWT_SECRET).
 *
 * O segredo vem da variável de ambiente JWT_SECRET e deve ter pelo menos
 * 32 bytes. A aplicação não inicia sem essa configuração.
 */
public class JwtUtil {

    private static final long VALIDADE_MS = 8 * 60 * 60 * 1000; // 8 horas

    private static final SecretKey CHAVE = carregarChave();

    /** Força a validação do segredo durante a inicialização da aplicação. */
    public static void validarConfiguracao() {
        // A inicialização da classe valida JWT_SECRET.
    }

    private static SecretKey carregarChave() {
        String secretEnv = System.getenv("JWT_SECRET");
        if (secretEnv == null || secretEnv.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET é obrigatório e deve conter pelo menos 32 bytes.");
        }
        return Keys.hmacShaKeyFor(secretEnv.getBytes(StandardCharsets.UTF_8));
    }

    public static String gerarToken(String username, String papel, Integer funcionarioId) {
        long agora = System.currentTimeMillis();
        return Jwts.builder()
            .subject(username)
            .claim("papel", papel)
            .claim("funcionarioId", funcionarioId)
            .issuedAt(new Date(agora))
            .expiration(new Date(agora + VALIDADE_MS))
            .signWith(CHAVE)
            .compact();
    }

    /** Lança exceção se o token for inválido ou expirado. */
    public static Claims validarToken(String token) {
        return Jwts.parser()
            .verifyWith(CHAVE)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
