package com.duoc.bank.cuentas.dto;

/**
 * Estadisticas del microservicio de cuentas: total de cuentas distintas
 * en la tabla legada (sin importar los snapshots semanales).
 */
public record StatsDTO(long cuentasDistintas) {
}
