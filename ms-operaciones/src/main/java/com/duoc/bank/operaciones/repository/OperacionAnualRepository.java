package com.duoc.bank.operaciones.repository;

import com.duoc.bank.operaciones.model.OperacionAnual;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de las operaciones anuales asociadas a cada cuenta.
 */
@Repository
public interface OperacionAnualRepository extends JpaRepository<OperacionAnual, Long> {

    /** Todas las operaciones anuales de una cuenta, de la mas reciente a la mas antigua. */
    List<OperacionAnual> findByCuentaIdOrderByFechaDesc(Long cuentaId);

    /** Variante paginada (sirve para limitar a las N mas recientes). */
    List<OperacionAnual> findByCuentaIdOrderByFechaDesc(Long cuentaId, Pageable pageable);
}
