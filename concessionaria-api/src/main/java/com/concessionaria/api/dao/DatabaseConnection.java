package com.concessionaria.api.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Igual ao DatabaseConnection do desktop, com uma diferença: aqui as
 * credenciais são lidas PRIMEIRO das variáveis de ambiente (DB_URL, DB_USER,
 * DB_PASSWORD) -- é assim que o Render vai fornecer a senha do banco em
 * produção, sem ela nunca aparecer em nenhum arquivo.
 *
 * Em desenvolvimento local, se as variáveis de ambiente não existirem,
 * caímos de volta para resources/config.properties (mesmo padrão do desktop).
 */
public class DatabaseConnection {

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver PostgreSQL não encontrado no classpath.", e);
        }

        URL      = System.getenv("DB_URL");
        USER     = System.getenv("DB_USER");
        PASSWORD = System.getenv("DB_PASSWORD");

        if (URL == null || USER == null || PASSWORD == null) {
            carregarDeArquivoLocal();
        }
    }

    private static void carregarDeArquivoLocal() {
        try (InputStream input = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new RuntimeException(
                    "Nem variáveis de ambiente (DB_URL/DB_USER/DB_PASSWORD) nem " +
                    "resources/config.properties foram encontrados. " +
                    "Copie config.properties.example -> config.properties e preencha, " +
                    "ou defina as variáveis de ambiente.");
            }

            Properties props = new Properties();
            props.load(input);

            URL      = props.getProperty("db.url");
            USER     = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler config.properties", e);
        }
    }

    /** Cria as tabelas caso ainda não existam. Chame uma única vez no boot. */
    public static void initialize() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS carros (
                    id     SERIAL PRIMARY KEY,
                    nome   VARCHAR(255) NOT NULL,
                    modelo VARCHAR(255) NOT NULL,
                    ano    INT          NOT NULL,
                    preco  DOUBLE PRECISION NOT NULL,
                    status VARCHAR(50)  NOT NULL DEFAULT 'Disponível',
                    nivel  VARCHAR(50)  NOT NULL
                )
                """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS funcionarios (
                    id      SERIAL PRIMARY KEY,
                    nome    VARCHAR(255)     NOT NULL,
                    salario DOUBLE PRECISION NOT NULL
                )
                """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id             SERIAL PRIMARY KEY,
                    username       VARCHAR(50)  UNIQUE NOT NULL,
                    senha          VARCHAR(255) NOT NULL,
                    papel          VARCHAR(20)  NOT NULL,
                    funcionario_id INT          REFERENCES funcionarios(id)
                )
                """);

            // Bancos criados pelo desktop antigo: garante a coluna de vínculo
            stmt.executeUpdate("""
                ALTER TABLE usuarios
                ADD COLUMN IF NOT EXISTS funcionario_id INT REFERENCES funcionarios(id)
                """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS vendas (
                    id             SERIAL PRIMARY KEY,
                    id_carro       INT              NOT NULL REFERENCES carros(id),
                    id_funcionario INT              NOT NULL REFERENCES funcionarios(id),
                    valor_venda    DOUBLE PRECISION NOT NULL,
                    comissao       DOUBLE PRECISION NOT NULL,
                    data_venda     DATE             NOT NULL
                )
                """);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
