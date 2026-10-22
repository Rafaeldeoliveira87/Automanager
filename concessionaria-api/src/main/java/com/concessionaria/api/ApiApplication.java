package com.concessionaria.api;

import com.concessionaria.api.dao.DatabaseConnection;
import com.concessionaria.api.service.JwtUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ponto de entrada da API.
 *
 * @SpringBootApplication faz 3 coisas de uma vez:
 *   1. Liga o "component scan" -- o Spring procura por todas as classes
 *      anotadas (@RestController, @Service etc.) neste pacote e nos
 *      subpacotes, e as registra automaticamente.
 *   2. Liga o "auto configuration" -- como detectamos o driver do Postgres
 *      no pom.xml, o Spring já prepara um DataSource sozinho.
 *   3. Sobe um servidor Tomcat embutido -- diferente do desktop Swing,
 *      aqui não existe "janela"; o programa fica vivo escutando uma porta
 *      HTTP (por padrão 8080) esperando requisições.
 */
@SpringBootApplication
public class ApiApplication {

    public static void main(String[] args) {
        JwtUtil.validarConfiguracao();
        try {
            DatabaseConnection.initialize();
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível conectar ao banco ou inicializar o schema. API não iniciada.", e);
        }
        SpringApplication.run(ApiApplication.class, args);
    }

    /**
     * Um endpoint mínimo só para confirmar que o servidor está de pé.
     * Depois de rodar, acesse: http://localhost:8080/status
     */
    @RestController
    static class StatusController {
        @GetMapping("/status")
        public String status() {
            return "API da Concessionária no ar!";
        }
    }
}
