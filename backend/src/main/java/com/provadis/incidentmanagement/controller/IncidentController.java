package com.provadis.incidentmanagement.controller;

import com.provadis.incidentmanagement.model.Incident;
import com.provadis.incidentmanagement.repository.IncidentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentRepository repository;

    public IncidentController(IncidentRepository repository) {
        this.repository = repository;
    }

    // GET /api/incidents -> alle Incidents
    @GetMapping
    public List<Incident> getAll() {
        return repository.findAll();
    }

    // POST /api/incidents -> Incident anlegen
    @PostMapping
    public Incident create(@RequestBody Incident incident) {
        // optional: Defaults setzen, falls leer
        if (incident.getStatus() == null)
            incident.setStatus("OPEN");
        if (incident.getPriority() == null)
            incident.setPriority("MEDIUM");
        return repository.save(incident);
    }

    // PATCH /api/incidents/{id}/status -> nur Status ändern
    @PatchMapping("/{id}/status")
    public Incident updateStatus(@PathVariable Long id, @RequestBody String status) {
        Incident incident = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found: " + id));

        // falls Postman JSON-String sendet ("RESOLVED"), entfernen wir
        // Anführungszeichen
        String cleaned = status.replace("\"", "");
        incident.setStatus(cleaned);

        return repository.save(incident);
    }

    // PATCH /api/incidents/{id}/priority -> nur Priority ändern
    @PatchMapping("/{id}/priority")
    public Incident updatePriority(@PathVariable Long id, @RequestBody String priority) {
        Incident incident = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found: " + id));

        String cleaned = priority.replace("\"", "");
        incident.setPriority(cleaned);

        return repository.save(incident);
    }

    @GetMapping("/{id}")
    public Incident getById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found: " + id));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Incident not found: " + id);
        }
        repository.deleteById(id);
    }

}