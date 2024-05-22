package com.cms.incidentmanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TicketDto {
    private Integer id;
    private String subject;
    private String type;
    private String issueRelated;
    private String priority;
    private String status;
    private String description;


}
