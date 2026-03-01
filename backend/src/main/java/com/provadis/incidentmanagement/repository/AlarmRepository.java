package com.provadis.incidentmanagement.repository;

import com.provadis.incidentmanagement.model.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
}