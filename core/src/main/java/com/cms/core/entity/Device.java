package com.cms.core.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Shashidhar on 4/18/2024.
 */

@Entity
@Getter
@Setter
@Table(name = "device")
public class Device implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "uid", nullable = false, length = 30)
    private String uid;

    @Column(name = "lat", nullable = false)
    private double lat;

    @Column(name = "lon", nullable = false)
    private double lon;

    @Column(name = "description", length = 250)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User user;

    @OneToOne(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private CustomerProductMappingDevice customerProductMappingDevice;

    @OneToOne(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private AreaDeviceMapping areaDeviceMapping;

}
