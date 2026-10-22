package dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Gerencia a conexão com o banco de dados.
 *
 * SEGURANÇA: prioriza config.properties ao lado do JAR ou em resources/.
 * O arquivo com credenciais não deve ser empacotado nem versionado.
 *
 * PERFORMANCE: initSchema() é executado apenas uma vez na inicialização
 * da aplicação (via DatabaseConnection.initialize()), não a cada conexão.
 */
public class DatabaseConnection {

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    // Carrega o driver e as configurações uma única vez
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "Driver PostgreSQL não encontrado no classpath. Verifique o Build Path.", e);
        }

        try (InputStream input = abrirConfiguracao()) {

            if (input == null) {
                throw new RuntimeException(
                    "Arquivo config.properties não encontrado.\n" +
                    "Copie config.properties.example para config.properties ao lado do JAR " +
                    "e preencha suas credenciais.");
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

    private static InputStream abrirConfiguracao() throws IOException {
        Path[] caminhos = {
            Path.of("config.properties"),
            Path.of("resources", "config.properties")
        };
        for (Path caminho : caminhos) {
            if (Files.isRegularFile(caminho)) {
                return Files.newInputStream(caminho);
            }
        }
        return DatabaseConnection.class.getClassLoader().getResourceAsStream("config.properties");
    }

    /**
     * Deve ser chamado UMA ÚNICA VEZ na inicialização da aplicação (no main).
     * Cria as tabelas caso ainda não existam.
     */
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
                    id     SERIAL PRIMARY KEY,
                    nome   VARCHAR(255)     NOT NULL,
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

            // Bancos criados antes desta versão: garante a coluna de vínculo
            stmt.executeUpdate("""
                ALTER TABLE usuarios
                ADD COLUMN IF NOT EXISTS funcionario_id INT REFERENCES funcionarios(id)
                """);

            // Foreign Keys garantem integridade referencial
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

    /** Retorna uma conexão aberta. O chamador é responsável por fechá-la (use try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
