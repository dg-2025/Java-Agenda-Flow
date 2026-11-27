package br.com.fatec.agendaflow.persistencia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Conexão com o banco, no mesmo espírito da classe {@code Banco} das aulas
 * (dados de conexão estáticos + conectar/desconectar).
 * <p>
 * A diferença é o banco: em vez do MariaDB do XAMPP, usamos o <b>SQLite</b>,
 * que é um arquivo. Assim o executável funciona em qualquer computador sem
 * instalar servidor nenhum. O arquivo fica em {@code %APPDATA%/AgendaFlow}.
 */
public final class Banco {

    /** AGENDAFLOW_DB permite apontar para outro arquivo (usado na demonstração automática). */
    private static Path arquivo = System.getenv("AGENDAFLOW_DB") != null
            ? Paths.get(System.getenv("AGENDAFLOW_DB"))
            : pastaDados().resolve("agendaflow.db");

    private Banco() {
    }

    /** Pasta onde ficam o banco e o histórico. */
    public static Path pastaDados() {
        String appData = System.getenv("APPDATA");
        Path base = (appData != null && !appData.isBlank())
                ? Paths.get(appData, "AgendaFlow")
                : Paths.get(System.getProperty("user.home"), ".agendaflow");
        return base;
    }

    /** Troca o arquivo do banco (usado pelos testes, que rodam num banco temporário). */
    public static void usarArquivo(Path novoArquivo) {
        arquivo = novoArquivo;
    }

    public static Path getArquivo() {
        return arquivo;
    }

    /** Abre uma conexão nova. Quem chama fecha (try-with-resources). */
    public static Connection conectar() throws SQLException {
        try {
            Files.createDirectories(arquivo.toAbsolutePath().getParent());
        } catch (IOException e) {
            throw new SQLException("Não foi possível criar a pasta do banco: " + e.getMessage(), e);
        }
        Properties props = new Properties();
        props.setProperty("foreign_keys", "true");
        return DriverManager.getConnection("jdbc:sqlite:" + arquivo.toAbsolutePath(), props);
    }

    public static void desconectar(Connection conexao) throws SQLException {
        if (conexao != null && !conexao.isClosed()) {
            conexao.close();
        }
    }

    /** Cria as tabelas (se ainda não existirem) e coloca os dados de exemplo no primeiro uso. */
    public static void inicializar() throws SQLException {
        criarEstrutura();
        DadosIniciais.popularSeVazio();
    }

    /** Só as tabelas, sem dados (os testes partem de um banco vazio). */
    public static void criarEstrutura() throws SQLException {
        try (Connection c = conectar(); Statement st = c.createStatement()) {
            for (String sql : Esquema.CRIAR_TABELAS) {
                st.execute(sql);
            }
        }
    }
}
