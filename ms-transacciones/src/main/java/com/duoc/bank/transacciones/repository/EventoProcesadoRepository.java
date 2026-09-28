package com.duoc.bank.transacciones.repository;

import com.duoc.bank.transacciones.model.EventoProcesado;
import com.duoc.bank.transacciones.model.EventoProcesadoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Acceso a la tabla de marcas de idempotencia del consumidor de eventos. */
@Repository
public interface EventoProcesadoRepository extends JpaRepository<EventoProcesado, EventoProcesadoId> {

    boolean existsByEventoIdAndConsumidor(String eventoId, String consumidor);
}
