package com.cms.incidentmanagement.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class RoleDto {
    private Integer id;

    private String name;

    private Integer createdBy;

    private List<Integer> featureIds;
}
