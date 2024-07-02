package com.cms.incidentmanagement.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by Shashidhar on 5/13/2024.
 */
@Getter
@Setter
public class AreaDto {

    private Integer id;

    private String name;

    private Integer zoneId;

    private List<Integer> userIds;

    private String polygon;

    private String userName;
}
