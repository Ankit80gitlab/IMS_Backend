package com.cms.incidentmanagement.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Shashidhar on 6/4/2024.
 */
@Getter
@Setter
public class SearchCriteriaTicketDto {

    private String subject;

    private String issueRelated;

    private String status;

    private String type;

    private String assignedTo;

    private String customerName;

    private String productName;

    private String deviceName;

    private String sortDirection;

    private Integer pageNo;

    private Integer pageSize;

}
