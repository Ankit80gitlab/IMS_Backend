package com.cms.incidentmanagement.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by Shashidhar on 6/14/2024.
 */
@Getter
@Setter
public class IncidentTypeDto {

    private Integer id;

    private List<Integer> defaultUserIds;

    private Integer escalationHour;

    private List<Integer> incidentEscalationUserIds;

    private String type;

    private String description;
}
