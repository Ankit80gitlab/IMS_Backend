package com.cms.core.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Shashidhar on 4/15/2024.
 */

@Entity
@Getter
@Setter
@Table(name = "customer")
public class Customer implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String state;

    private String city;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CustomerProductMapping> customerProductMappings;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<User> users;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User user;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private Set<Zone> zones;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private Set<Role> roles;
}


