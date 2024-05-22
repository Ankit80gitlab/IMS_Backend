package com.cms.core.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;

/**
 * Created by Shashidhar on 4/22/2024.
 */
@Entity
@Getter
@Setter
@Table(name = "area")
public class Area implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_time", nullable = false)
    private Timestamp createdTime;

    @Column(name = "polygon", nullable = false)
    private String polygon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User user;

    @OneToMany(mappedBy = "area", fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AreaUserMapping> areaUserMappings;

    @OneToMany(mappedBy = "area", fetch = FetchType.LAZY)
    private Set<AreaDeviceMapping> areaDeviceMappings;
}
