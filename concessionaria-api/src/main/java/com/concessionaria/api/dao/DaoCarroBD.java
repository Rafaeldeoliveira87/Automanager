package com.concessionaria.api.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.concessionaria.api.entidade.Carro;

public class DaoCarroBD {

    /** Usado pelo vendedor no app: só os carros que ainda podem ser vendidos. */
    public List<Carro> loadDisponiveis() {
        List<Carro> carros = new ArrayList<>();
        String sql = "SELECT * FROM carros WHERE status = 'Disponível' ORDER BY nome";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                carros.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar carros disponíveis: " + e.getMessage());
        }

        return carros;
    }

    /** Busca um carro específico por id -- usado ao registrar a venda, para pegar preço/nível atuais. */
    public Carro buscarPorId(int id) {
        String sql = "SELECT * FROM carros WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar carro: " + e.getMessage());
        }

        return null;
    }

    private Carro mapRow(ResultSet rs) throws SQLException {
        return new Carro(
            rs.getInt("id"),
            rs.getString("nome"),
            rs.getString("modelo"),
            rs.getInt("ano"),
            rs.getDouble("preco"),
            rs.getString("status"),
            rs.getString("nivel")
        );
    }
}
