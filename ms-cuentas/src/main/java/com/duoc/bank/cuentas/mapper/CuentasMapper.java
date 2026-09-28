package com.duoc.bank.cuentas.mapper;

import com.duoc.bank.common.dto.SaldoCuentaDTO;
import com.duoc.bank.cuentas.dto.CuentaCompletaDTO;
import com.duoc.bank.cuentas.model.Cuenta;

/**
 * Conversiones entidad -&gt; DTO del microservicio de cuentas, centralizadas
 * en un solo lugar. Asi los DTOs quedan como POJOs planos y la logica de
 * transformacion no se repite entre controladores y servicios.
 */
public final class CuentasMapper {

    private CuentasMapper() {
    }

    public static CuentaCompletaDTO toCuentaCompletaDTO(Cuenta cuenta) {
        CuentaCompletaDTO dto = new CuentaCompletaDTO();
        dto.setCuentaId(cuenta.getCuentaId());
        dto.setNombre(cuenta.getNombre());
        dto.setSaldo(cuenta.getSaldo());
        dto.setSaldoFinal(cuenta.getSaldoFinal());
        dto.setEdad(cuenta.getEdad());
        dto.setTipo(cuenta.getTipo());
        return dto;
    }

    public static SaldoCuentaDTO toSaldoCuentaDTO(Cuenta cuenta) {
        return new SaldoCuentaDTO(cuenta.getCuentaId(), cuenta.getNombre(), cuenta.getSaldo());
    }
}
