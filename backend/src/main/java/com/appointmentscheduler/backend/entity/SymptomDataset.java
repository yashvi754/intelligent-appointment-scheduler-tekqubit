package com.appointmentscheduler.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "symptom_dataset")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomDataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "symptom_id")
    private Integer symptomId;

    @ManyToOne
    @JoinColumn(name = "mapped_service_id", nullable = false)
    private ServiceCatalog serviceCatalog;

    @Column(name = "keyword_phrase")
    private String keywordPhrase;
}