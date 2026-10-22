package com.concessionaria.api.controller;

import com.concessionaria.api.dao.DaoUsuario;
import com.concessionaria.api.dto.Dtos.ErroResponse;
import com.concessionaria.api.dto.Dtos.LoginRequest;
import com.concessionaria.api.dto.Dtos.LoginResponse;
import com.concessionaria.api.entidade.Usuario;
import com.concessionaria.api.service.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final DaoUsuario daoUsuario = new DaoUsuario();

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        Usuario u = daoUsuario.autenticar(req.username(), req.senha());

        if (u == null) {
            return ResponseEntity.status(401).body(new ErroResponse("Usuário ou senha inválidos."));
        }

        String token = JwtUtil.gerarToken(u.getUsername(), u.getPapel(), u.getFuncionarioId());

        return ResponseEntity.ok(
            new LoginResponse(token, u.getUsername(), u.getPapel(), u.getFuncionarioId())
        );
    }
}
