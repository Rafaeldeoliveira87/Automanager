package com.concessionaria.api.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.concessionaria.api.entidade.Vendas;

public class DaoVendasBD {

    /**
     * Mesma lógica do desktop: insere a venda e marca o carro como
     * "Vendido" na mesma transação (ou os dois acontecem, ou nenhum).
     */
    public void registrarVenda(Vendas venda) throws SQLException {
        String sqlVenda  = "INSERT INTO vendas (id_carro, id_funcionario, valor_venda, comissao, data_venda) VALUES (?,?,?,?,?)";
        String sqlStatus = "UPDATE carros SET status = 'Vendido' WHERE id = ? AND status = 'Disponível'";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pVenda = conn.prepareStatement(sqlVenda)) {
                pVenda.setInt(1, venda.getIdCarro());
                pVenda.setInt(2, venda.getIdFuncionario());
                pVenda.setDouble(3, venda.getValorvenda());
                pVenda.setDouble(4, venda.getComissao());
                pVenda.setDate(5, Date.valueOf(venda.getDataVenda()));
                pVenda.executeUpdate();
            }

            int linhasAfetadas;
            try (PreparedStatement pStatus = conn.prepareStatement(sqlStatus)) {
                pStatus.setInt(1, venda.getIdCarro());
                linhasAfetadas = pStatus.executeUpdate();
            }

            // Se 0 linhas foram afetadas, o carro já não estava mais "Disponível"
            // (por exemplo, dois vendedores tentaram vender o mesmo carro ao mesmo
            // tempo) -- desfazemos a venda inteira.
            if (linhasAfetadas == 0) {
                conn.rollback();
                throw new SQLException("Carro não está mais disponível para venda.");
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    /** Usado na tela "minhas vendas" do vendedor no app. */
    public List<Vendas> listarPorFuncionario(int funcionarioId) {
        List<Vendas> vendas = new ArrayList<>();
        String sql = "SELECT * FROM vendas WHERE id_funcionario = ? ORDER BY data_venda DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, funcionarioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    vendas.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar vendas do funcionário: " + e.getMessage());
        }

        return vendas;
    }

    private Vendas mapRow(ResultSet rs) throws SQLException {
        return new Vendas(
            rs.getInt("id"),
            rs.getInt("id_carro"),
            rs.getInt("id_funcionario"),
            rs.getDouble("valor_venda"),
            rs.getDouble("comissao"),
            rs.getDate("data_venda").toLocalDate()
        );
    }
}
