package com.provadis.incidentmanagement.repository;
import com.provadis.incidentmanagement.model.Alarm;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findBySourceOrderByCreatedAtDesc(String source);
}