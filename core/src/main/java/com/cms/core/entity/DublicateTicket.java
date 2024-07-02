package com.cms.core.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Shashidhar on 6/21/2024.
 */
@Entity
@Getter
@Setter
@Table(name = "dublicate_ticket")
public class DublicateTicket implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dublicate_ticket_id")
    private Ticket ticket;

}
