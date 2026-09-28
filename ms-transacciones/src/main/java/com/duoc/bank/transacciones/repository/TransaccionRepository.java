package com.duoc.bank.transacciones.repository;

import com.duoc.bank.transacciones.model.Transaccion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio del historial de transacciones del sistema legado.
 */
@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    Page<Transaccion> findByTipo(String tipo, Pageable pageable);

    Page<Transaccion> findByAnomaliaTrue(Pageable pageable);

    Page<Transaccion> findByTipoAndAnomaliaTrue(String tipo, Pageable pageable);

    /** Ultimas 10 transacciones, de la mas reciente a la mas antigua. */
    List<Transaccion> findTop10ByOrderByFechaDescRegistroIdDesc();

    /** Total de transacciones marcadas como anomalia. */
    long countByAnomaliaTrue();

    /** Mayor identificador de negocio legado, para asignar el siguiente al registrar. */
    @Query("select coalesce(max(t.id), 0) from Transaccion t")
    Long maxIdLegado();
}
