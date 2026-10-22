package com.concessionaria.api.filter;

import com.concessionaria.api.service.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Roda uma vez para cada requisição HTTP que chega, ANTES de ela alcançar
 * o Controller. Pense nele como o "segurança na porta": confere o crachá
 * (token) antes de deixar entrar.
 *
 * Rotas públicas (login, status) passam direto. As demais precisam do
 * header:  Authorization: Bearer <token>
 *
 * Se o token for válido, guardamos username/papel/funcionarioId como
 * "atributos da requisição" -- os Controllers leem esses atributos depois,
 * sem precisar decodificar o token de novo.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String[] ROTAS_PUBLICAS = { "/auth/login", "/status" };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String caminho = request.getRequestURI();
        for (String rotaPublica : ROTAS_PUBLICAS) {
            if (caminho.equals(rotaPublica)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            responderNaoAutorizado(response, "Token ausente. Envie o header Authorization: Bearer <token>.");
            return;
        }

        String token = header.substring("Bearer ".length());

        try {
            Claims claims = JwtUtil.validarToken(token);
            request.setAttribute("username", claims.getSubject());
            request.setAttribute("papel", claims.get("papel", String.class));

            // Lido como Number e convertido manualmente: a biblioteca JWT pode
            // decodificar o número como Integer ou Long dependendo do valor,
            // e um cast direto para Integer quebraria no segundo caso.
            Object funcionarioIdRaw = claims.get("funcionarioId");
            Integer funcionarioId = (funcionarioIdRaw != null)
                ? ((Number) funcionarioIdRaw).intValue()
                : null;
            request.setAttribute("funcionarioId", funcionarioId);

        } catch (Exception e) {
            responderNaoAutorizado(response, "Token inválido ou expirado. Faça login novamente.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void responderNaoAutorizado(HttpServletResponse response, String mensagem) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"mensagem\": \"" + mensagem + "\"}");
    }
}
