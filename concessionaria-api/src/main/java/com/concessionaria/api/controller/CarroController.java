package com.concessionaria.api.controller;

import com.concessionaria.api.dao.DaoCarroBD;
import com.concessionaria.api.entidade.Carro;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/carros")
public class CarroController {

    private final DaoCarroBD daoCarro = new DaoCarroBD();

    /**
     * GET /carros/disponiveis
     * Não precisa checar papel: tanto GERENTE quanto VENDEDOR podem ver
     * a lista (o JwtAuthFilter já garante que existe um token válido --
     * é o suficiente aqui).
     */
    @GetMapping("/disponiveis")
    public ResponseEntity<List<Carro>> listarDisponiveis() {
        return ResponseEntity.ok(daoCarro.loadDisponiveis());
    }
}
