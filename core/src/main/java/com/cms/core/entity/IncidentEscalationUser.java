package com.cms.core.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;


/**
 * Created by Shashidhar on 6/11/2024.
 */
@Entity
@Getter
@Setter
@Table(name = "incident_escalation_user")
public class IncidentEscalationUser implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "incident_escalation_id")
    private IncidentEscalation incidentEscalation;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
