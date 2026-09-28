package com.duoc.bank.operaciones.client;

import com.duoc.bank.common.dto.CargoRequest;
import com.duoc.bank.common.dto.SaldoCuentaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Cliente HTTP declarativo hacia ms-cuentas (endpoints internos).
 * El nombre logico {@code ms-cuentas} se resuelve via Eureka (service discovery)
 * con balanceo de carga del lado del cliente. Los errores HTTP se traducen a las
 * excepciones de negocio compartidas por {@link CuentasClientConfig}.
 */
@FeignClient(name = "ms-cuentas", configuration = CuentasClientConfig.class)
public interface CuentasClient {

    /** Saldo vigente de una cuenta (registro mas reciente). */
    @GetMapping("/api/internal/cuentas/{cuentaId}/saldo")
    SaldoCuentaDTO obtenerSaldo(@PathVariable("cuentaId") Long cuentaId);

    /** Registra un cargo (descuento de saldo) sobre la cuenta. */
    @PostMapping("/api/internal/cuentas/{cuentaId}/cargos")
    SaldoCuentaDTO registrarCargo(@PathVariable("cuentaId") Long cuentaId, @RequestBody CargoRequest cargo);
}
