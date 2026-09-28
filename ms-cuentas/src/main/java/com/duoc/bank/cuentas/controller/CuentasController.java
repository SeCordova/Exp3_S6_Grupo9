package com.duoc.bank.cuentas.controller;

import com.duoc.bank.common.dto.SaldoCuentaDTO;
import com.duoc.bank.cuentas.dto.CuentaCompletaDTO;
import com.duoc.bank.cuentas.dto.PaginaDTO;
import com.duoc.bank.cuentas.dto.StatsDTO;
import com.duoc.bank.cuentas.mapper.CuentasMapper;
import com.duoc.bank.cuentas.service.CuentasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API publica del microservicio de cuentas: listado paginado, detalle de la
 * cuenta vigente, saldo y estadisticas. Las operaciones anuales no se exponen
 * aqui: pertenecen a otro microservicio.
 *
 * El controlador solo orquesta HTTP: la logica de negocio vive en
 * {@link CuentasService} y la conversion a DTO en {@link CuentasMapper}.
 * Los errores los resuelve de forma centralizada {@code GlobalExceptionHandler}.
 */
@Validated
@RestController
@RequestMapping("/api/cuentas")
@Tag(name = "Cuentas", description = "Saldos, listado paginado y estadisticas de las cuentas vigentes")
public class CuentasController {

    private final CuentasService cuentasService;

    public CuentasController(CuentasService cuentasService) {
        this.cuentasService = cuentasService;
    }

    /** Listado paginado de cuentas distintas (registro mas reciente de cada una). */
    @GetMapping
    @Operation(summary = "Listado paginado de cuentas vigentes")
    public PaginaDTO<CuentaCompletaDTO> listarCuentas(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("cuentaId"));
        return PaginaDTO.desde(cuentasService.listarCuentasVigentes(pageable), CuentasMapper::toCuentaCompletaDTO);
    }

    /** Detalle de la cuenta vigente (sin operaciones anuales: son de otro servicio). */
    @GetMapping("/{cuentaId}")
    @Operation(summary = "Detalle de la cuenta vigente")
    public CuentaCompletaDTO detalleCuenta(@PathVariable Long cuentaId) {
        return CuentasMapper.toCuentaCompletaDTO(cuentasService.obtenerCuentaVigente(cuentaId));
    }

    /** Saldo vigente de una cuenta. */
    @GetMapping("/{cuentaId}/saldo")
    @Operation(summary = "Saldo vigente de una cuenta")
    public SaldoCuentaDTO obtenerSaldo(@PathVariable Long cuentaId) {
        return cuentasService.obtenerSaldo(cuentaId);
    }

    /** Estadisticas del servicio: total de cuentas distintas (requiere permiso REPORTES). */
    @GetMapping("/stats")
    @Operation(summary = "Estadisticas de cuentas (requiere permiso REPORTES)")
    public StatsDTO stats() {
        return new StatsDTO(cuentasService.contarCuentasDistintas());
    }
}
