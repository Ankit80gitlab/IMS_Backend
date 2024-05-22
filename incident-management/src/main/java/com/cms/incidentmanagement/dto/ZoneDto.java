package com.cms.incidentmanagement.dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Shashidhar on 5/13/2024.
 */
@Getter
@Setter
public class ZoneDto {
    private Integer id;
    private String name;
    private String polygon;
    private Integer customerId;
    private HashMap<String,Object> customer;
}
