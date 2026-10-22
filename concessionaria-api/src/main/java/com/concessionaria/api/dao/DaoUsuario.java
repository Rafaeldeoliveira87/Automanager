package com.concessionaria.api.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.concessionaria.api.entidade.Usuario;
import com.concessionaria.api.service.PasswordUtil;

public class DaoUsuario {

    public Usuario autenticar(String username, String senhaPura) {
        String sql = "SELECT * FROM usuarios WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashArmazenado = rs.getString("senha");
                    if (PasswordUtil.verificarSenha(senhaPura, hashArmazenado)) {
                        return mapRow(rs);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao autenticar usuário: " + e.getMessage());
        }

        return null;
    }

    private Usuario mapRow(ResultSet rs) throws SQLException {
        int funcionarioId = rs.getInt("funcionario_id");
        Integer funcionarioIdObj = rs.wasNull() ? null : funcionarioId;

        return new Usuario(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("papel"),
            funcionarioIdObj
        );
    }
}
