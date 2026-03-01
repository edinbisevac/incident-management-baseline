package com.provadis.incidentmanagement.controller;

import com.provadis.incidentmanagement.model.Alarm;
import com.provadis.incidentmanagement.model.Incident;
import com.provadis.incidentmanagement.repository.AlarmRepository;
import com.provadis.incidentmanagement.repository.IncidentRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/alarms")
public class AlarmController {

    private final AlarmRepository alarmRepository;
    private final IncidentRepository incidentRepository;

    public AlarmController(AlarmRepository alarmRepository, IncidentRepository incidentRepository) {
        this.alarmRepository = alarmRepository;
        this.incidentRepository = incidentRepository;
    }

    @PostMapping
    public Incident receiveAlarm(@RequestBody Alarm alarm) {
        // 1) Alarm vervollständigen + speichern
        if (alarm.getCreatedAt() == null) {
            alarm.setCreatedAt(Instant.now());
        }
        Alarm savedAlarm = alarmRepository.save(alarm);

        // 2) Korrelation (MVP):
        // Wenn es bereits einen OPEN-Incident für dieselbe Source gibt ->
        // aktualisieren,
        // sonst neuen Incident anlegen.
        Incident incident = incidentRepository
                .findFirstBySourceAndStatus(savedAlarm.getSource(), "OPEN")
                .orElseGet(Incident::new);

        // Falls neu:
        if (incident.getId() == null) {
            incident.setSource(savedAlarm.getSource());
            incident.setTitle("Alarm von " + savedAlarm.getSource());
            incident.setStatus("OPEN");
            incident.setPriority("MEDIUM");
        }

        // Immer aktualisieren:
        incident.setDescription(savedAlarm.getMessage());

        // Priority ggf. erhöhen (HIGH > MEDIUM > LOW)
        String mappedPriority = mapSeverityToPriority(savedAlarm.getSeverity());
        incident.setPriority(maxPriority(incident.getPriority(), mappedPriority));

        return incidentRepository.save(incident);
    }

    @GetMapping
    public List<Alarm> getAllAlarms() {
        return alarmRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteAlarm(@PathVariable Long id) {
        if (!alarmRepository.existsById(id)) {
            throw new RuntimeException("Alarm not found: " + id);
        }
        alarmRepository.deleteById(id);
    }

    @DeleteMapping
    public void deleteAllAlarms() {
        alarmRepository.deleteAll();
    }

    private String mapSeverityToPriority(String severity) {
        if (severity == null)
            return "MEDIUM";
        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> "HIGH";
            case "MAJOR" -> "MEDIUM";
            case "MINOR" -> "LOW";
            default -> "MEDIUM";
        };
    }

    private String maxPriority(String a, String b) {
        int pa = priorityValue(a);
        int pb = priorityValue(b);
        return (pb > pa) ? b : a;
    }

    private int priorityValue(String p) {
        if (p == null)
            return 2;
        return switch (p.toUpperCase()) {
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 2;
        };
    }
}