package com.cms.core.entity;

import lombok.Getter; import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by Shashidhar on 4/18/2024.
 */
@Entity
@Getter @Setter
@Table(name = "customer_product_mapping_device")
public class CustomerProductMappingDevice implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "customerProductMappingDevice", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Ticket> tickets;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_product_mapping_id")
    private CustomerProductMapping customerProductMapping;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;


}
