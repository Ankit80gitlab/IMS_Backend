package com.cms.incidentmanagement.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class UserDto {

    private Integer id;

    private String username;

    private String name;

    private String email;

    private String password;

    private Integer roleId;

    private Integer customerId;

    private List<Integer> productIds;
}
