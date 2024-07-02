package com.cms.core.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by Shashidhar on 6/11/2024.
 */
@Entity
@Getter
@Setter
@Table(name = "user_type")
public class UserType implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @OneToMany(mappedBy = "userType", fetch = FetchType.LAZY)
    private Set<User> users;

}
