package com.provadis.incidentmanagement.repository;

import com.provadis.incidentmanagement.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findFirstBySourceAndStatus(String source, String status);
}