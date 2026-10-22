package com.concessionaria.api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Gera e valida tokens JWT.
 *
 * Um JWT tem 3 partes (header.payload.signature). O "payload" carrega
 * informações (aqui: username, papel e funcionarioId) e a "signature"
 * garante que ninguém alterou esse conteúdo sem saber o segredo (JWT_SECRET).
 *
 * O segredo vem da variável de ambiente JWT_SECRET. Em produção (Render),
 * configure isso no painel. Em desenvolvimento local, se a variável não
 * existir, geramos uma só para a sessão atual (o token para de funcionar
 * a cada restart -- é aceitável em dev, NUNCA em produção).
 */
public class JwtUtil {

    private static final long VALIDADE_MS = 8 * 60 * 60 * 1000; // 8 horas

    private static final SecretKey CHAVE = carregarChave();

    private static SecretKey carregarChave() {
        String secretEnv = System.getenv("JWT_SECRET");
        if (secretEnv != null && secretEnv.length() >= 32) {
            return Keys.hmacShaKeyFor(secretEnv.getBytes());
        }
        System.err.println("[AVISO] JWT_SECRET não definido (ou curto demais). " +
            "Gerando uma chave temporária só para esta execução -- " +
            "configure a variável de ambiente antes de ir para produção.");
        return Keys.hmacShaKeyFor(java.util.Base64.getEncoder()
            .encode(java.util.UUID.randomUUID().toString().repeat(2).getBytes()));
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
