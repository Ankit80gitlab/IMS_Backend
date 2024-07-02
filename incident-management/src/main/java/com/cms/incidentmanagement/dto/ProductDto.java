package com.cms.incidentmanagement.dto;

import com.cms.core.entity.IncidentEscalationUser;
import com.cms.core.entity.IncidentType;
import com.cms.core.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by Shashidhar on 4/15/2024.
 */
@Getter
@Setter
public class ProductDto {

    private Integer id;

    private String productName;

    private String productDescription;

    private String productType;

    private List<IncidentTypeDto> incidentTypeDtos;

    private List<Integer> userIds;

}

