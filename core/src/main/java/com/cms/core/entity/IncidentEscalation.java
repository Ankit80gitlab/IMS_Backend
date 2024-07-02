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
@Table(name = "incident_escalation")
public class IncidentEscalation implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer escalationHour;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User user;

    @ManyToOne
    @JoinColumn(name = "incident_type_id")
    private IncidentType incidentType;

    @ManyToOne
    @JoinColumn(name = "customer_product_mapping_id")
    private CustomerProductMapping customerProductMapping;

    @OneToMany(mappedBy = "incidentEscalation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IncidentEscalationUser> incidentEscalationUsers;

    @OneToMany(mappedBy = "incidentEscalation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IncidentEscalationDefaultUser> incidentEscalationDefaultUsers;

}
