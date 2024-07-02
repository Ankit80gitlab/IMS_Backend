package com.cms.incidentmanagement.dto;

import com.cms.core.entity.Product;
import lombok.*;

import java.util.List;

/**
 * Created by Shashidhar on 4/15/2024.
 */
@Getter
@Setter
public class CustomerDto {

    private Integer id;

    private String name;

    private String state;

    private String city;

    private List<Integer> productIds;
}

