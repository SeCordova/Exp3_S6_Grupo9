package com.duoc.bank.cuentas.repository;

import com.duoc.bank.cuentas.model.Cuenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de cuentas. Como la tabla guarda snapshots semanales del mismo
 * {@code cuenta_id}, todas las consultas de "estado actual" usan el registro
 * mas reciente (mayor {@code registro_id}) de cada cuenta.
 */
@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    /** Estado vigente de una cuenta: el registro mas reciente de ese cuenta_id. */
    Optional<Cuenta> findFirstByCuentaIdOrderByRegistroIdDesc(Long cuentaId);

    /** Cuentas distintas (solo el registro mas reciente de cada una), paginadas. */
    @Query("SELECT c FROM Cuenta c WHERE c.registroId IN "
            + "(SELECT MAX(c2.registroId) FROM Cuenta c2 GROUP BY c2.cuentaId)")
    Page<Cuenta> findCuentasVigentes(Pageable pageable);

    /** Cuentas distintas (solo el registro mas reciente de cada una), sin paginar. */
    @Query("SELECT c FROM Cuenta c WHERE c.registroId IN "
            + "(SELECT MAX(c2.registroId) FROM Cuenta c2 GROUP BY c2.cuentaId)")
    List<Cuenta> findCuentasVigentes();

    /** Total de cuentas distintas en la tabla (sin importar los snapshots). */
    @Query("SELECT COUNT(DISTINCT c.cuentaId) FROM Cuenta c")
    long contarCuentasDistintas();
}
