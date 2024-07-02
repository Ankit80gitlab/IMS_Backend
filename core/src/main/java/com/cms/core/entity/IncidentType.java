package com.cms.core.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Shashidhar on 6/11/2024.
 */
@Entity
@Getter
@Setter
@Table(name = "incident_type")
public class IncidentType implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String type;

    private String description;

    @OneToMany(mappedBy = "incidentType", fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "incidentType", fetch = FetchType.LAZY)
    private List<IncidentEscalation> incidentEscalations;

}
