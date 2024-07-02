package com.cms.incidentmanagement.dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

/**
 * Created by Shashidhar on 5/13/2024.
 */
@Getter
@Setter
public class DeviceDto {

    private Integer id;

    private String uid;

    private String name;

    private double lat;

    private double lon;

    private String description;

    private Integer productId;

    private Integer areaId;
}
