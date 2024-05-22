package com.cms.core.entity;

/**
 * Created by Shashidhar on 4/18/2024.
 */

import lombok.Getter; import lombok.Setter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(name = "ticket_updation_history")
public class TicketUpdationHistory implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "updated_time", nullable = false)
    private Timestamp updatedTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "change_in", nullable = false)
    private String changeIn;

    @Column(name = "change_from", nullable = false)
    private String changeFrom;

    @Column(name = "change_to", nullable = false)
    private String changeTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;


}
