package com.cms.core.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "ticket")
public class Ticket implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String subject;

    private String issueRelated;

    private String priority;

    private String status;

    private String description;

    private Timestamp createdTime;

    private Boolean isDuplicate = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_product_mapping_device_id")
    private CustomerProductMappingDevice customerProductMappingDevice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_product_mapping_id")
    private CustomerProductMapping customerProductMapping;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TicketFile> ticketFiles;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TicketUpdationHistory> ticketUpdationHistory;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments;

    @ManyToOne
    @JoinColumn(name = "type")
    private IncidentType incidentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_ticket_id")
    private Ticket parentTicket;

    @OneToMany(mappedBy = "parentTicket", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Ticket> subTickets;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_ticket_id")
    private Ticket originalTicket;

    @OneToMany(mappedBy = "originalTicket", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Ticket> duplicateTickets;
}
