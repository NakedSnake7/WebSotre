package com.WeedTitlan.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.WeedTitlan.server.model.MensajePendiente;



@Repository
public interface MensajePendienteRepository extends JpaRepository<MensajePendiente, Long> {
    List<MensajePendiente> findTop10ByEnviadoFalseOrderByCreadoEnAsc();
}

