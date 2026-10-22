package com.concessionaria.api.service;

public class ComissaoService {

    /**
     * Básico → 2% | Médio → 5% | Luxo → 8%
     * IMPORTANTE: esse cálculo roda só no servidor. O app do celular nunca
     * envia o valor da comissão -- ele manda só o id do carro, e o servidor
     * calcula. Assim ninguém consegue forjar uma comissão maior manipulando
     * a requisição.
     */
    public static double calcularComissao(double preco, String nivelCarro) {
        if (nivelCarro == null) return 0;

        return switch (nivelCarro.toLowerCase()) {
            case "basico", "básico" -> preco * 0.02;
            case "medio",  "médio"  -> preco * 0.05;
            case "luxo"             -> preco * 0.08;
            default -> 0;
        };
    }
}
