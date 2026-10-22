package com.concessionaria.api.controller;

import com.concessionaria.api.dao.DaoCarroBD;
import com.concessionaria.api.dao.DaoVendasBD;
import com.concessionaria.api.dto.Dtos.ErroResponse;
import com.concessionaria.api.dto.Dtos.VendaRequest;
import com.concessionaria.api.dto.Dtos.VendaResponse;
import com.concessionaria.api.entidade.Carro;
import com.concessionaria.api.entidade.Vendas;
import com.concessionaria.api.service.ComissaoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/vendas")
public class VendaController {

    private final DaoCarroBD daoCarro = new DaoCarroBD();
    private final DaoVendasBD daoVendas = new DaoVendasBD();

    /**
     * POST /vendas
     * Só quem logou como VENDEDOR pode registrar venda -- checamos o
     * "papel" que o JwtAuthFilter guardou como atributo da requisição
     * (ele veio de dentro do token, ninguém pode forjar isso sem saber
     * o JWT_SECRET do servidor).
     */
    @PostMapping
    public ResponseEntity<?> registrarVenda(@RequestBody VendaRequest req, HttpServletRequest request) {
        String papel = (String) request.getAttribute("papel");
        Integer funcionarioId = (Integer) request.getAttribute("funcionarioId");

        if (!"VENDEDOR".equals(papel)) {
            return ResponseEntity.status(403).body(new ErroResponse("Apenas vendedores registram vendas."));
        }
        if (funcionarioId == null) {
            return ResponseEntity.status(400).body(new ErroResponse(
                "Este usuário VENDEDOR não está vinculado a um funcionário. Peça ao gerente para corrigir o cadastro."));
        }

        Carro carro = daoCarro.buscarPorId(req.carroId());
        if (carro == null || !"Disponível".equals(carro.getStatus())) {
            return ResponseEntity.status(400).body(new ErroResponse("Carro indisponível para venda."));
        }

        double comissao = ComissaoService.calcularComissao(carro.getPreco(), carro.getNivel());

        Vendas venda = new Vendas(0, carro.getId(), funcionarioId, carro.getPreco(), comissao, LocalDate.now());

        try {
            daoVendas.registrarVenda(venda);
        } catch (SQLException e) {
            return ResponseEntity.status(409).body(new ErroResponse(e.getMessage()));
        }

        return ResponseEntity.ok(new VendaResponse(carro.getNome(), carro.getPreco(), comissao));
    }

    /** GET /vendas/minhas -- o vendedor vê só as próprias vendas. */
    @GetMapping("/minhas")
    public ResponseEntity<?> minhasVendas(HttpServletRequest request) {
        Integer funcionarioId = (Integer) request.getAttribute("funcionarioId");
        if (funcionarioId == null) {
            return ResponseEntity.status(400).body(new ErroResponse("Usuário sem vínculo de funcionário."));
        }

        List<Vendas> vendas = daoVendas.listarPorFuncionario(funcionarioId);
        return ResponseEntity.ok(vendas);
    }
}
