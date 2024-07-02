package com.cms.core.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by Shashidhar on 4/17/2024.
 */
@Entity
@Getter
@Setter
@Table(name = "customer_product_mapping")
public class CustomerProductMapping implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "customerProductMapping", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Ticket> tickets;

    @OneToMany(mappedBy = "customerProductMapping", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CustomerProductMappingDevice> customerProductMappingDevices;

    @OneToMany(mappedBy = "customerProductMapping", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserProductMapping> userProductMappings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "customerProductMapping", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<IncidentEscalation> incidentEscalations;

}

