package com.provadis.incidentmanagement.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String source;     // z.B. "router-12"
    private String message;    // z.B. "Link down"
    private String severity;   // z.B. "CRITICAL", "MAJOR", "MINOR"
    private Instant createdAt; // Zeitpunkt des Alarms
}