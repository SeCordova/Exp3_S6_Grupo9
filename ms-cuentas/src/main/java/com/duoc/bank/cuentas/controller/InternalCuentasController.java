package com.duoc.bank.cuentas.controller;

import com.duoc.bank.common.dto.CargoRequest;
import com.duoc.bank.common.dto.SaldoCuentaDTO;
import com.duoc.bank.cuentas.service.CuentasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints internos inter-servicio del microservicio de cuentas.
 * Los consume ms-operaciones via Feign al orquestar un retiro. Van protegidos
 * solo por el API key (sin JWT), por lo que el interceptor JWT excluye
 * {@code /api/internal/**}.
 */
@RestController
@RequestMapping("/api/internal/cuentas")
@Tag(name = "Interno", description = "Endpoints inter-servicio (solo API key; los consume ms-operaciones via Feign)")
public class InternalCuentasController {

    private final CuentasService cuentasService;

    public InternalCuentasController(CuentasService cuentasService) {
        this.cuentasService = cuentasService;
    }

    /** Saldo vigente de una cuenta, para validacion previa de otros servicios. */
    @GetMapping("/{cuentaId}/saldo")
    @Operation(summary = "Saldo vigente de una cuenta (uso interno)")
    public SaldoCuentaDTO obtenerSaldo(@PathVariable Long cuentaId) {
        return cuentasService.obtenerSaldo(cuentaId);
    }

    /**
     * Registra un cargo sobre el saldo vigente y devuelve el saldo restante.
     * El registro del historial de la operacion lo hace ms-operaciones.
     */
    @PostMapping("/{cuentaId}/cargos")
    @Operation(summary = "Registra un cargo y devuelve el saldo restante (uso interno)")
    public SaldoCuentaDTO registrarCargo(@PathVariable Long cuentaId,
                                         @Valid @RequestBody CargoRequest request) {
        return cuentasService.registrarCargo(cuentaId, request.getMonto());
    }
}
