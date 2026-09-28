package com.duoc.bank.operaciones.controller;

import com.duoc.bank.operaciones.dto.OperacionLigeraDTO;
import com.duoc.bank.operaciones.dto.RetiroRequest;
import com.duoc.bank.operaciones.dto.RetiroResponse;
import com.duoc.bank.operaciones.dto.StatsDTO;
import com.duoc.bank.operaciones.service.OperacionesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST del microservicio de operaciones.
 * Expone el retiro de dinero (orquestado con ms-cuentas), el historial de
 * operaciones anuales de una cuenta y estadisticas del servicio.
 *
 * La validacion de la entrada es declarativa (@Valid sobre RetiroRequest) y
 * los errores los resuelve de forma centralizada {@code GlobalExceptionHandler}:
 * 400 validacion, 404 cuenta inexistente, 409 saldo insuficiente,
 * 503 servicio dependiente no disponible.
 */
@RestController
@RequestMapping("/api/operaciones")
@Tag(name = "Operaciones", description = "Retiros y registro de operaciones anuales")
public class OperacionesController {

    private final OperacionesService operacionesService;

    public OperacionesController(OperacionesService operacionesService) {
        this.operacionesService = operacionesService;
    }

    /**
     * Retiro de dinero. Validaciones: monto obligatorio y positivo (400),
     * cuenta existente (404) y saldo suficiente (409).
     */
    @PostMapping("/retiro")
    @Operation(summary = "Ejecuta un retiro de dinero (requiere permiso RETIRO)")
    public ResponseEntity<RetiroResponse> retirar(@Valid @RequestBody RetiroRequest request) {
        return ResponseEntity.ok(operacionesService.retirar(request.getCuentaId(), request.getMonto()));
    }

    /** Historial de operaciones anuales de una cuenta (las N mas recientes). */
    @GetMapping("/cuenta/{cuentaId}")
    @Operation(summary = "Lista las operaciones anuales de una cuenta (limite entre 1 y 20)")
    public ResponseEntity<List<OperacionLigeraDTO>> listarOperaciones(
            @PathVariable Long cuentaId,
            @RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(operacionesService.listarOperaciones(cuentaId, limite));
    }

    /** Estadisticas del servicio: total de operaciones anuales registradas. */
    @GetMapping("/stats")
    @Operation(summary = "Estadisticas del servicio (requiere permiso REPORTES)")
    public ResponseEntity<StatsDTO> estadisticas() {
        return ResponseEntity.ok(new StatsDTO(operacionesService.contarOperaciones()));
    }
}
