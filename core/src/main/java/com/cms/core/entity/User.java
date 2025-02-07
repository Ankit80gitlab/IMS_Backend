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
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    private String name;

    private String email;

    private String password;

    private Timestamp createdTime;

    private Timestamp loginTime;

    private Boolean ldapUser = false;

    @Column(name = "lat", nullable = false)
    private double lat;

    @Column(name = "lon", nullable = false)
    private double lon;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRoleMapping> userRoleMappings;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserProductMapping> userProductMappings;

    @OneToMany(mappedBy = "createdUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Ticket> tickets;

    @OneToMany(mappedBy = "assignedTo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Ticket> assignedTickets;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User user;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<AreaUserMapping> areaUserMappings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type")
    private UserType userType;

}


