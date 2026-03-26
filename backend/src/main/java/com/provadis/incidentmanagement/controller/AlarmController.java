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
        if (alarm.getCreatedAt() == null) {
            alarm.setCreatedAt(Instant.now());
        }

        Alarm savedAlarm = alarmRepository.save(alarm);

        Incident incident = incidentRepository
                .findFirstBySourceAndStatus(savedAlarm.getSource(), "OPEN")
                .orElseGet(Incident::new);

        String mappedPriority = mapSeverityToPriority(savedAlarm.getSeverity());

        if (incident.getId() == null) {
            incident.setSource(savedAlarm.getSource());
            incident.setTitle("Alarm von " + savedAlarm.getSource());
            incident.setStatus("OPEN");
            incident.setPriority(mappedPriority);
        } else {
            incident.setPriority(maxPriority(incident.getPriority(), mappedPriority));
        }

        // Letzte Nachricht weiterhin direkt im Incident sichtbar
        incident.setDescription(savedAlarm.getMessage());

        return incidentRepository.save(incident);
    }

    @GetMapping
    public List<Alarm> getAlarms(@RequestParam(required = false) String source) {
        if (source != null && !source.isBlank()) {
            return alarmRepository.findBySourceOrderByCreatedAtDesc(source);
        }
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
        if (severity == null) {
            return "MEDIUM";
        }
        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> "HIGH";
            case "MAJOR" -> "MEDIUM";
            case "MINOR" -> "LOW";
            default -> "MEDIUM";
        };
    }

    private String maxPriority(String current, String incoming) {
        int currentValue = priorityValue(current);
        int incomingValue = priorityValue(incoming);
        return incomingValue > currentValue ? incoming : current;
    }

    private int priorityValue(String priority) {
        if (priority == null) return 0;

        return switch (priority.toUpperCase()) {
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }
}