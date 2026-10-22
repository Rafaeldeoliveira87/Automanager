package com.concessionaria.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Records = uma forma enxuta do Java (desde a versão 16) de criar classes
 * que só carregam dados, sem lógica. O Java gera sozinho o construtor,
 * os getters e o equals/hashCode. Cada record aqui vira automaticamente
 * um objeto JSON quando entra/sai da API (o Spring faz essa conversão).
 */
public class Dtos {

    /** Corpo esperado em POST /auth/login */
    public record LoginRequest(
        @NotBlank(message = "username é obrigatório") String username,
        @NotBlank(message = "senha é obrigatória") String senha
    ) {}

    /** Resposta de POST /auth/login */
    public record LoginResponse(
        String token,
        String username,
        String papel,
        Integer funcionarioId
    ) {}

    /**
     * Corpo esperado em POST /vendas.
     * Repare: só o id do carro. O valor da venda e a comissão são
     * calculados no servidor -- nunca confiamos em número vindo do app.
     */
    public record VendaRequest(int carroId) {}

    /** Resposta de POST /vendas */
    public record VendaResponse(
        String carroNome,
        double valorVenda,
        double comissao
    ) {}

    /** Formato padrão de mensagem de erro devolvida pela API */
    public record ErroResponse(String mensagem) {}
}
