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
@Table(name="ticket")
public class Ticket implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String subject;

    private String type;

    private String issueRelated;

    private String priority;

    private String status;

    private String description;

    private Timestamp createdTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_product_mapping_device_id")
    private CustomerProductMappingDevice customerProductMappingDevice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_product_mapping_id")
    private CustomerProductMapping customerProductMapping;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private Set<TicketFile> ticketFiles;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private Set<TicketUpdationHistory> ticketUpdationHistory;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private Set<Comment> comments;

}
